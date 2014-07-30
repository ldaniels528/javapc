package ibmpc.devices.cpu.x86.decoder;

import static ibmpc.devices.cpu.x86.decoder.DecoderUtil.lookupReferencedAddress;
import static ibmpc.devices.cpu.x86.decoder.DecoderUtil.lookupSecondaryOperand;
import static ibmpc.devices.cpu.x86.opcodes.data.ESC.ESC_09;
import static ibmpc.devices.cpu.x86.opcodes.data.ESC.ESC_19;
import static ibmpc.devices.cpu.x86.opcodes.data.ESC.ESC_1C;
import static ibmpc.devices.cpu.x86.opcodes.data.ESC.ESC_1E;
import static ibmpc.devices.cpu.x86.opcodes.data.ESC.ESC_29;
import static ibmpc.devices.cpu.x86.opcodes.data.ESC.ESC_39;
import ibmpc.devices.cpu.Intel80x86;
import ibmpc.devices.cpu.OpCode;
import ibmpc.devices.cpu.operands.ByteValue;
import ibmpc.devices.cpu.operands.Operand;
import ibmpc.devices.cpu.operands.memory.DWordPtr;
import ibmpc.devices.cpu.operands.memory.MemoryReference;
import ibmpc.devices.cpu.operands.memory.QWordPtr;
import ibmpc.devices.cpu.operands.memory.WordPtr;
import ibmpc.devices.cpu.x86.opcodes.bitwise.RCL;
import ibmpc.devices.cpu.x86.opcodes.bitwise.RCR;
import ibmpc.devices.cpu.x86.opcodes.bitwise.ROL;
import ibmpc.devices.cpu.x86.opcodes.bitwise.ROR;
import ibmpc.devices.cpu.x86.opcodes.bitwise.SAL;
import ibmpc.devices.cpu.x86.opcodes.bitwise.SAR;
import ibmpc.devices.cpu.x86.opcodes.bitwise.SHL;
import ibmpc.devices.cpu.x86.opcodes.bitwise.SHR;
import ibmpc.devices.cpu.x86.opcodes.data.DB;
import ibmpc.devices.cpu.x86.opcodes.data.DW;
import ibmpc.devices.cpu.x86.opcodes.data.ESC;
import ibmpc.devices.cpu.x86.opcodes.data.XLAT;
import ibmpc.devices.cpu.x86.opcodes.math.AAD;
import ibmpc.devices.cpu.x86.opcodes.math.AAM;
import ibmpc.devices.cpu.x87.opcodes.FADD;
import ibmpc.devices.cpu.x87.opcodes.FBSTP;
import ibmpc.devices.cpu.x87.opcodes.FCOM;
import ibmpc.devices.cpu.x87.opcodes.FCOMP;
import ibmpc.devices.cpu.x87.opcodes.FDIV;
import ibmpc.devices.cpu.x87.opcodes.FDIVR;
import ibmpc.devices.cpu.x87.opcodes.FIADD;
import ibmpc.devices.cpu.x87.opcodes.FICOM;
import ibmpc.devices.cpu.x87.opcodes.FICOMP;
import ibmpc.devices.cpu.x87.opcodes.FIDIV;
import ibmpc.devices.cpu.x87.opcodes.FIDIVR;
import ibmpc.devices.cpu.x87.opcodes.FILD;
import ibmpc.devices.cpu.x87.opcodes.FIMUL;
import ibmpc.devices.cpu.x87.opcodes.FIST;
import ibmpc.devices.cpu.x87.opcodes.FISTP;
import ibmpc.devices.cpu.x87.opcodes.FISUB;
import ibmpc.devices.cpu.x87.opcodes.FISUBR;
import ibmpc.devices.cpu.x87.opcodes.FLD;
import ibmpc.devices.cpu.x87.opcodes.FLDCW;
import ibmpc.devices.cpu.x87.opcodes.FLDENV;
import ibmpc.devices.cpu.x87.opcodes.FMUL;
import ibmpc.devices.cpu.x87.opcodes.FRSTOR;
import ibmpc.devices.cpu.x87.opcodes.FSAVE;
import ibmpc.devices.cpu.x87.opcodes.FST;
import ibmpc.devices.cpu.x87.opcodes.FSTCW;
import ibmpc.devices.cpu.x87.opcodes.FSTENV;
import ibmpc.devices.cpu.x87.opcodes.FSTP;
import ibmpc.devices.cpu.x87.opcodes.FSTSW;
import ibmpc.devices.cpu.x87.opcodes.FSUB;
import ibmpc.devices.cpu.x87.opcodes.FSUBR;
import ibmpc.devices.memory.X86MemoryProxy;

/**
 * <pre>
 * Decodes instruction code between D0h and DFh 
 * --------------------------------------------------------------------------- 
 *	type	size (bits)	description 				comments
 * --------------------------------------------------------------------------- 
 * 	t		2			register/memory type		register=11b
 * 	j		3			instruction sub-code	
 * 	r		3			register/memory reference	
 * 	i		5	 		instruction type  	
 * 	k		1			function Code		
 * 	s		1			source type					'cl'=1, '1'=0
 * 	c		1	 		memory class 	 			8-bit=0, 16-bit=1	
 * 	d		16/32		offset 
 * 
 * --------------------------------------------------------------------------- 
 * Type A
 * instruction 					code	tt jjj rrr	iiiii k s c dddd
 * ---------------------------------------------------------------------------
 * shl byte ptr [bx],1			27D0	00 100 111	11010 0 0 0 
 * rol al,1						C0D0	11 000 000	11010 0 0 0 
 * rol cl,1						C1D0	11 000 001	11010 0 0 0 
 * ror al,1						C8D0	11 001 000	11010 0 0 0 
 * ror cl,1						C9D0	11 001 001	11010 0 0 0 
 * shl al,1						E0D0	11 100 000	11010 0 0 0 
 * shl cl,1						E1D0	11 100 001	11010 0 0 0 
 * shr al,1						E8D0	11 101 000	11010 0 0 0 
 * shr cl,1						E9D0	11 101 001	11010 0 0 0 
 * sar cl,1						F9D0	11 111 001	11010 0 0 0 
 * sar word ptr [si],1			3CD1	00 111 100	11010 0 0 1 
 * shl byte ptr [bx],cl			27D2	00 100 111	11010 0 1 0 
 * shl byte ptr [si+nnnn],cl	A4D2	10 100 100	11010 0 1 0 nnnn
 * rcl al,cl					D0D2	11 010 000	11010 0 1 0 
 * rcl cl,cl					D1D2	11 010 001	11010 0 1 0 
 * rcr al,cl					D8D2	11 011 000	11010 0 1 0 
 * rcr cl,cl					D9D2	11 011 001	11010 0 1 0 
 * shl al,cl					E0D2	11 100 000	11010 0 1 0 
 * shl cl,cl					E1D2	11 100 001	11010 0 1 0 
 * shl ax,cl					E0D3	11 100 000	11010 0 1 1 
 * 
 * --------------------------------------------------------------------------- 
 * Type B
 * instruction 					code	tt rrr mmm 	iiiii j kk
 * ---------------------------------------------------------------------------
 * aam 							0AD4	00 001 010	11010 1 00 	
 * aad							0AD5	00 001 010	11010 1 01
 * (undefined)					  D6				11010 1 10
 * xlat							  D7				11010 1 11
 * 
 * --------------------------------------------------------------------------- 
 * Type C
 * instruction 					code	tt jjj rrr	iiiii c kk dddd
 * ---------------------------------------------------------------------------
 * fadd  dword ptr [bx]			07D8	00 000 111	11011 0 00
 * fmul  dword ptr [bx]			0FD8	00 001 111	11011 0 00
 * fcom	 dword ptr [bx]			17D8	00 010 111	11011 0 00
 * fcomp dword ptr [bx]			1FD8	00 011 111	11011 0 00
 * fsub  dword ptr [bx]			27D8	00 100 111	11011 0 00
 * fsubr dword ptr [bx]			2FD8	00 101 111	11011 0 00
 * fdiv  dword ptr [bx]			37D8	00 110 111	11011 0 00
 * fdivr dword ptr [bx]			3FD8	00 111 111	11011 0 00
 * 
 * fld   dword ptr [bx]			07D9	00 000 111	11011 0 01
 * esc   09,[bx][bx]			0FD9	00 001 111	11011 0 01
 * fst   dword ptr [bx]			17D9	00 010 111	11011 0 01
 * fstp  dword ptr [bx]			1FD9	00 011 111	11011 0 01
 * fldenv [bx]					27D9	00 100 111	11011 0 01
 * fldcw  [bx]					2FD9	00 101 111	11011 0 01
 * fstenv [bx]					37D9	00 110 111	11011 0 01
 * fstcw  [bx]					3FD9	00 111 111	11011 0 01
 * 
 * fiadd  dword ptr [bx]		07DA	00 000 111	11011 0 10
 * fimul  dword ptr [bx]		0FDA	00 001 111	11011 0 10
 * ficom  dword ptr [bx]		17DA	00 010 111	11011 0 10
 * ficomp dword ptr [bx]		1FDA	00 011 111	11011 0 10
 * fisub  dword ptr [bx]		27DA	00 100 111	11011 0 10
 * fisubr dword ptr [bx]		2FDA	00 101 111	11011 0 10
 * fidiv  dword ptr [bx]		37DA	00 110 111	11011 0 10
 * fidivr dword ptr [bx]		3FDA	00 111 111	11011 0 10
 * 
 * fild  dword ptr [bx]			07DB	00 000 111	11011 0 11
 * esc   19,[bx]tbyte ptr [bx]	0FDB	00 001 111	11011 0 11
 * fist  dword ptr [bx]			17DB	00 010 111	11011 0 11
 * fistp dword ptr [bx]			1FDB	00 011 111	11011 0 11
 * esc   1C,[bx]tbyte ptr [bx]	27DB	00 100 111	11011 0 11
 * fld   tbyte ptr [bx]			2FDB	00 101 111	11011 0 11
 * esc   1E,[bx]tbyte ptr [bx]	37DB	00 110 111	11011 0 11
 * fstp  tbyte ptr [bx]			3FDB	00 111 111	11011 0 11
 * 
 * fadd  qword ptr [bx]			07DC	00 000 111	11011 1 00
 * fmul  qword ptr [bx]			0FDC	00 001 111	11011 1 00
 * fcom  qword ptr [bx]			17DC	00 010 111	11011 1 00
 * fcomp qword ptr [bx]			1FDC	00 011 111	11011 1 00
 * fsub  qword ptr [bx]			27DC	00 100 111	11011 1 00
 * fsubr qword ptr [bx]			2FDC	00 101 111	11011 1 00
 * fdiv  qword ptr [bx]			37DC	00 110 111	11011 1 00
 * fdivr qword ptr [bx]			3FDC	00 111 111	11011 1 00
 * 
 * fld   qword ptr [bx]			07DD	00 000 111	11011 1 01
 * esc   29,[bx][bx]			0FDD	00 001 111	11011 1 01
 * fst   qword ptr [bx]			17DD	00 010 111	11011 1 01
 * fstp  qword ptr [bx]			1FDD	00 011 111	11011 1 01
 * frstor [bx]					27DD	00 100 111	11011 1 01
 * esc    2D,[bx][bx]			2FDD	00 101 111	11011 1 01
 * fsave  [bx]					37DD	00 110 111	11011 1 01
 * fstsw  [bx]					3FDD	00 111 111	11011 1 01
 * 
 * fiadd  word ptr [bx]			07DE 	00 000 111	11011 1 10 
 * fimul  word ptr [bx]			0FDE 	00 001 111	11011 1 10 
 * ficom  word ptr [bx]			17DE 	00 010 111	11011 1 10 
 * ficomp word ptr [bx]			1FDE 	00 011 111	11011 1 10
 * fisub  word ptr [bx]			27DE	00 100 111	11011 1 10
 * fisubr word ptr [bx]			2FDE	00 101 111	11011 1 10
 * fidiv  word ptr [bx]			37DE	00 110 111	11011 1 10
 * fidivr word ptr [bx]			3FDE	00 111 111	11011 1 10
 *
 * fild   word ptr [bx]			07DF	00 000 111	11011 1 11
 * esc    39,[bx]tbyte ptr [bx]	0FDF	00 001 111	11011 1 11
 * fist   word ptr [bx]			17DF	00 010 111	11011 1 11
 * fistp  word ptr [bx]			1FDF	00 011 111	11011 1 11
 * fbld   tbyte ptr [bx]		27DF	00 100 111	11011 1 11
 * fild   qword ptr [bx]		2FDF	00 101 111	11011 1 11
 * fbstp  tbyte ptr [bx]		37DF	00 110 111	11011 1 11
 * fistp  qword ptr [bx]		3FDF	00 111 111	11011 1 11
 * </pre>
 * @author lawrence.daniels@gmail.com
 */
public class DecoderD0 implements Decoder {
	// define the instruction code
	private static final int INSTRUCTION_CODE_A = 0x34; // 110100
	private static final int INSTRUCTION_CODE_B = 0x35; // 110101
	private static final int INSTRUCTION_CODE_C = 0x36; // 110110
	private static final int INSTRUCTION_CODE_D = 0x37; // 110111
	
	// define the instruction sub codes
	private static final int ROL = 0; // 000b
	private static final int ROR = 1; // 001b
	private static final int RCL = 2; // 010b
	private static final int RCR = 3; // 011b
	private static final int SHL = 4; // 100b
	private static final int SHR = 5; // 101b
	private static final int SAL = 6; // 110b
	private static final int SAR = 7; // 111b

	/* (non-Javadoc)
	 * @see ibmpc.devices.cpu.decoders.I8086Decoder#decode(ibmpc.devices.cpu.VirtualCPU)
	 */
	public OpCode decode( final Intel80x86 cpu, final X86MemoryProxy proxy ) {
		// Instruction code layout
		// -----------------------------
		// fedc ba98 7654 3210 (16 bits)
		// ttjj jrrr iiii iasc 
		
		// get the next word
		final int code8 = proxy.nextByte();
		
		// extract the instruction code
		// code: .... .... iiii ia.. (mask = 0000 0000 1111 1100)
		final int insCode = ( ( code8 & 0x00FC ) >> 2 );
		
		// decode the appropriate instruction type
		switch( insCode ) {
			case INSTRUCTION_CODE_A: 	return decodeTypeA( cpu, proxy, code8 );
			case INSTRUCTION_CODE_B: 	return decodeTypeB( cpu, proxy, code8 );
			case INSTRUCTION_CODE_C: 	return decodeTypeC( cpu, proxy, code8 );
			case INSTRUCTION_CODE_D:	return decodeTypeC( cpu, proxy, code8 );
			default:					throw new UnhandledByteCodeException( code8 );
		}
	}
	
	/**
	 * Interprets Type A instructions (ROL,ROR,RCL,RCR,SHL,SHR,SAL,SAR)
	 * @param cpu the given {@link Intel80x86 CPU} instance
	 * @param code8 the given 8-bit instruction code
	 * @return true, if the instruction was sucessfully decodeed
	 */
	private OpCode decodeTypeA( final Intel80x86 cpu, final X86MemoryProxy proxy, final int code8 ) {
		// Instruction code layout
		// -----------------------------
		// fedc ba98 7654 3210 (16 bits)
		// ttjj jrrr iiii iasc 
		
		// get the 16-bit instruction code
		final int code16 = proxy.nextWord( code8 );
		
		// extract the instruction sub code bits 
		// code: ..jj j... .... .... (mask = 0011 1000 0000 0000)
		final int subCode = ( code16 & 0x3800 ) >> 11;
		
		// extract the source type bit (0='1', 1='cl')
		// code: .... .... .... ..s. (mask = 0000 0000 0000 0010)
		final boolean isCL = ( ( code16 & 0x0002 ) >> 1 ) == 1;
		
		// lookup the source and target
		final Operand dest	= lookupSecondaryOperand( cpu, proxy, code16 ); 
		final Operand src	= isCL ? cpu.CL : ByteValue.ONE;
		
		// evaluate the instruction
		switch( subCode ) {
			case ROL: 	return new ROL( dest, src ); 
			case ROR: 	return new ROR( dest, src ); 
			case RCL: 	return new RCL( dest, src ); 
			case RCR: 	return new RCR( dest, src ); 
			case SHL: 	return new SHL( dest, src ); 
			case SHR: 	return new SHR( dest, src ); 
			case SAL: 	return new SAL( dest, src ); 
			case SAR: 	return new SAR( dest, src ); 
			default:	throw new UnhandledByteCodeException( code16 );
		}
	}
	
	/**
	 * Interprets Type B instructions
	 * @param cpu the given {@link Intel80x86 CPU} instance
	 * @param code8 the given 8-bit instruction code
	 * @return true, if the instruction was sucessfully decodeed
	 * @see AAM
	 * @see AAD
	 * @see XLAT
	 */
	private OpCode decodeTypeB( final Intel80x86 cpu, final X86MemoryProxy proxy, final int code8 ) {
		int code16;
		switch( code8 ) {
			// AAM
			case 0xD4:	
				code16 = proxy.nextWord( code8 );
				return ( code16 == 0x0AD4 ) ? AAM.getInstance() : new DW( code16 );
			// AAD
			case 0xD5:	
				code16 = proxy.nextWord( code8 );
				return ( code16 == 0x0AD5 ) ? AAD.getInstance() : new DW( code16 );
			// (undefined)
			case 0xD6:	
				return new DB( code8 );
			// XLAT
			case 0xD7:	
				return XLAT.getInstance(); 
			// shouldn't happen
			default:	
				throw new UnhandledByteCodeException( code8 );
		}
	}
	
	/**
	 * Interprets 80x87 (Type "C") instructions
	 * @param cpu the given {@link Intel80x86 CPU} instance
	 * @param code8 the given 8-bit instruction code
	 * @return true, if the instruction was sucessfully decodeed
	 */
	private OpCode decodeTypeC( final Intel80x86 cpu, final X86MemoryProxy proxy, final int code8 ) {
		// Instruction code layout
		// -----------------------------
		// fedc ba98 7654 3210 (16 bits)
		// ttjj jrrr iiii ickk
		
		// get the 16-bit instruction code
		final int code16 = proxy.nextWord( code8 );
		
		// get the instruction sub-code 
		// code16: ..jj j... .... .... (mask = 0011 1000 0000 0000) 
		final int subCode = ( code16 & 0x3800 ) >> 11;
		
		// get the composite (element + reference) code
		// code16: tt.. .rrr .... .... (mask = 1100 0111 0000 0000) 
		final int compCode = ( code16 & 0xC700 ) >> 8;
		
		// get the memory class
		// code16: .... .... .... .c.. (mask = 0000 0000 0000 0100)
		final int memClass = ( code16 & 0x0004 ) >> 2;
		final boolean low = ( memClass == 0 );
		
		// get the instruction group code
		// code16: .... .... .... ..kk (mask = 0000 0000 0000 0011) 
		final int groupCode = ( code16 & 0x0003 );
		
		// get the memory reference
		final MemoryReference memRef = lookupReferencedAddress( cpu, proxy, compCode );
		
		// get the RAM instance
		Operand operand;
		
		// decode the instruction
		switch( groupCode ) {
			// group #1 (kk = 00b)
			case 0x00:
				operand = low ? new DWordPtr( memRef ) : new QWordPtr( memRef );
				switch( subCode ) {
					case 0x00: return new FADD( operand ); 
					case 0x01: return new FMUL( operand ); 
					case 0x02: return new FCOM( operand ); 
					case 0x03: return new FCOMP( operand ); 
					case 0x04: return new FSUB( operand ); 
					case 0x05: return new FSUBR( operand ); 
					case 0x06: return new FDIV( operand ); 
					case 0x07: return new FDIVR( operand ); 
				}
				break;
		
			// group #2 (kk = 01b)
			case 0x01:
				operand = low ? new DWordPtr( memRef ) : new QWordPtr( memRef );
				switch( subCode ) {
					case 0x00: return new FLD( operand );
					case 0x01: return new ESC( low ? ESC_09 : ESC_29, operand );
					case 0x02: return new FST( operand );
					case 0x03: return new FSTP( operand );
					case 0x04: return low ? new FLDENV( operand ) : new FRSTOR( operand );
					case 0x05: return low ? new FLDCW( operand )  : new ESC( ESC_29, operand );
					case 0x06: return low ? new FSTENV( operand ) : new FSAVE( operand );
					case 0x07: return low ? new FSTCW( operand )  : new FSTSW( operand );
				}
				break;
				
			// group #3 (kk = 10b)
			case 0x02:
				operand = low ? new DWordPtr( memRef ) : new WordPtr( memRef );
				switch( subCode ) {
					case 0x00: return new FIADD( operand ); 
					case 0x01: return new FIMUL( operand ); 
					case 0x02: return new FICOM( operand ); 
					case 0x03: return new FICOMP( operand ); 
					case 0x04: return new FISUB( operand ); 
					case 0x05: return new FISUBR( operand ); 
					case 0x06: return new FIDIV( operand ); 
					case 0x07: return new FIDIVR( operand ); 
				}
				break;
			
			// group #4 (kk = 11b)
			case 0x03:
				operand = low ? new DWordPtr( memRef ) : new WordPtr( memRef );
				switch( subCode ) {
					case 0x00: return new FILD( operand );
					case 0x01: return new ESC( low ? ESC_19 : ESC_39, operand );
					case 0x02: return new FIST( operand );
					case 0x03: return new FISTP( operand );
					case 0x04: return new ESC( ESC_1C, operand );
					case 0x05: return new FLD( operand );
					case 0x06: return low ? new ESC( ESC_1E, operand ) : new FBSTP( operand );
					case 0x07: return new FSTP( operand );
				}
				break;
		}
		
		// if the instruction wasn't decoded, throw exception
		throw new UnhandledByteCodeException( code16 );
	}
	
}