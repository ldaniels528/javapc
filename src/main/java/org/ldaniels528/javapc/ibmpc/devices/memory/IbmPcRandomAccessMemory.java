package org.ldaniels528.javapc.ibmpc.devices.memory;

import org.apache.log4j.Logger;
import org.ldaniels528.javapc.ibmpc.devices.cpu.operands.ByteValue;
import org.ldaniels528.javapc.ibmpc.devices.cpu.operands.Operand;
import org.ldaniels528.javapc.ibmpc.devices.cpu.operands.WordValue;
import org.ldaniels528.javapc.util.ByteConversion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static org.ldaniels528.javapc.ibmpc.devices.memory.X86MemoryUtil.computePhysicalAddress;

/**
 * Represents IBM PC-style segmented random access memory.
 * @author lawrence.daniels@gmail.com
 */
public class IbmPcRandomAccessMemory {
    private static final int SYSTEM_MEMORY_SIZE = 0x100000;
    private final Logger logger = Logger.getLogger(getClass());
    private final List<RegisteredSegment> registeredSegments;
    private final byte[] systemMemory;

    ///////////////////////////////////////////////////////
    //      Constructor(s)
    ///////////////////////////////////////////////////////

    /**
     * Creates a new memory instance
     */
    public IbmPcRandomAccessMemory() {
        // create the linear memory array
        this.systemMemory = new byte[SYSTEM_MEMORY_SIZE];
        this.registeredSegments = new LinkedList<>();

        // randomize the bytes in memory
        //new Random(System.currentTimeMillis()).nextBytes(systemMemory);
    }

    public void add(final int addressStart,
                    final int addressEnd,
                    final MemorySegmentListener listener) {
        registeredSegments.add(new RegisteredSegment(addressStart, addressEnd, listener));
    }

    /**
     * Returns the size of memory (in kilobytes)
     * @return the size of memory
     */
    public int sizeInKilobytes() {
        return 640;
    }

    /**
     * Copies the specified number of bytes from the
     * given absolute source address to the given
     * absolute destination address within memory.
     * @param sourceAddress      the given absolute source address
     * @param destinationAddress the given absolute destination address
     * @param count              the specified number of bytes
     */
    public void copyBytes(final int sourceAddress,
                          final int destinationAddress,
                          final int count) {
        System.arraycopy(systemMemory, sourceAddress, systemMemory, destinationAddress, count);
        updateSegmentObservers(destinationAddress, destinationAddress + count);
    }

    /**
     * Copies the specified number of bytes from the
     * given source segment and offset to the given
     * destination segment and offset within memory.
     * @param srcSegment the given source segment
     * @param srcOffset  the given source offset
     * @param dstSegment the given destination segment
     * @param dstOffset  the given destination offset
     * @param count      the specified number of bytes
     */
    public void copyBytes(final int srcSegment, final int srcOffset,
                          final int dstSegment, final int dstOffset,
                          final int count) {
        // compute the source and destination positions within the array
        final int srcPos = computePhysicalAddress(srcSegment, srcOffset);
        final int destPos = computePhysicalAddress(dstSegment, dstOffset);

        // copy the data
        System.arraycopy(systemMemory, srcPos, systemMemory, destPos, count);
        updateSegmentObservers(destPos, destPos + count);
    }

    /**
     * Retrieves a byte from memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @return the {@link ByteValue byte} read from memory
     */
    public ByteValue getByte(final Operand segment, final Operand offset) {
        return new ByteValue(getByte(segment.get(), offset.get()));
    }

    /**
     * Retrieves a word from memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @return the {@link WordValue word} read from memory
     */
    public WordValue getWord(final Operand segment, final Operand offset) {
        return new WordValue(getByte(segment.get(), offset.get()));
    }

    /**
     * Retrieves a single byte from memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @return the byte read from memory
     */
    public int getByte(int segment, int offset) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // retrieve the byte from memory
        return ByteConversion.convertToInt(systemMemory[physicalAddress]);
    }

    /**
     * Retrieves a block of memory at the given offset
     * @param physicalAddress the physical address of the memory block
     * @param length          the length of the memory block
     * @return a block of memory at the given offset
     */
    public byte[] getBytes(final int physicalAddress, final int length) {
        // create an empty memory block
        final byte[] block = new byte[length];

        // copy the contents of physical memory into the block
        System.arraycopy(systemMemory, physicalAddress, block, 0, length);

        // return the memory block
        return block;
    }

    /**
     * Retrieves a block of memory at the given offset
     * @param segment the segment of the memory block
     * @param offset  the offset of the memory block
     * @param length  the length of the memory block
     * @return a block of memory at the given offset
     */
    public byte[] getBytes(final int segment, final int offset, final int length) {
        // get the linear address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // return the copied memory block
        return getBytes(physicalAddress, length);
    }

    /**
     * Retrieves a block of memory at the given offset
     * @param segment the segment of the memory block
     * @param offset  the initial offset of the memory block
     * @param length  the length of the memory block
     * @return a block of memory at the given offset
     */
    public long getBytesAsLong(final int segment, final int offset, final int length) {
        // get the linear address
        final int address = computePhysicalAddress(segment, offset);

        // convert to a long value
        long value = 0;
        for (int n = 0; n < length; n++) {
            value <<= 8;
            final int byteVal = systemMemory[address + n];
            value |= ((byteVal < 0) ? byteVal + 256 : byteVal);
        }

        // return the integer
        return value;
    }

    /**
     * Retrieves a block of memory at the given offset
     * @param segment the segment of the memory block
     * @param offset  the offset of the memory block
     * @param block   the block of binary data
     * @param length  the length of the memory block
     */
    public void getBytes(final int segment, final int offset, final byte[] block, final int length) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // copy the contents of physical memory into the block
        System.arraycopy(systemMemory, physicalAddress, block, 0, length);
    }

    /**
     * Retrieves a word (two bytes) from memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @return the word read from memory
     */
    public int getWord(final int segment, final int offset) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);
        if (physicalAddress >= systemMemory.length) {
            throw new IllegalStateException(format("Memory reference out of bounds %08X", physicalAddress));
        }

        // get the high and low order bytes
        int loByte = systemMemory[physicalAddress];
        int hiByte = systemMemory[physicalAddress + 1];

        // fix the signs
        if (loByte < 0) loByte += 256;
        if (hiByte < 0) hiByte += 256;

        // convert the bytes to a word
        return (hiByte << 8) | loByte;
    }

    /**
     * Retrieves a double word (two words) from memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @return the word read from memory
     */
    public int getDoubleWord(final int segment, final int offset) {
        // get the high and low order words
        final int hiWord = getWord(segment, offset);
        final int loWord = getWord(segment, offset + 2);

        // convert the bytes to a double word
        return (hiWord << 16) | loWord;
    }

    /**
     * Guarantees that each bit within the mask sets the
     * corresponding bit within the referenced byte of memory.
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @param mask    the given bit mask
     */
    public void setBits(int segment, int offset, int mask) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // AND the byte in memory
        systemMemory[physicalAddress] &= (byte) (0xFF - mask);
        systemMemory[physicalAddress] |= (byte) mask;
        updateSegmentObservers(physicalAddress, physicalAddress);
    }

    /**
     * Writes a single byte to memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @param b       the given byte
     */
    public void setByte(final int segment, final int offset, final int b) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // set the byte
        systemMemory[physicalAddress] = (byte) b;
        updateSegmentObservers(physicalAddress, physicalAddress);
    }

    /**
     * Writes a block of binary data to memory at the given segment and offset
     * @param physicalAddress the physical memory address
     * @param block           the block of binary data
     * @param length          the length of the memory block
     */
    public void setBytes(final int physicalAddress, final byte[] block, final int length) {
        // fail-safe check
        if (physicalAddress + length > SYSTEM_MEMORY_SIZE) {
            logger.debug(format("Warning physical address (%x) is outside of maximum (%x)", physicalAddress + length, SYSTEM_MEMORY_SIZE));
        }

        // copy the contents of the memory block to physical memory
        System.arraycopy(block, 0, systemMemory, physicalAddress, length);
        updateSegmentObservers(physicalAddress, physicalAddress + length);
    }

    /**
     * Writes a block of binary data to memory at the given segment and offset
     * @param segment the segment of the memory block
     * @param offset  the offset of the memory block
     * @param block   the block of binary data
     * @param length  the length of the memory block
     */
    public void setBytes(final int segment, final int offset, final byte[] block, final int length) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // copy the contents of the memory block to physical memory
        setBytes(physicalAddress, block, length);
        updateSegmentObservers(physicalAddress, physicalAddress + length);
    }

    /**
     * Writes a word (two bytes) to memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @param word    the given word
     */
    public void setWord(final int segment, final int offset, final int word) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // determine high and low bytes of the word
        final byte hiByte = (byte) ((word & 0xFF00) >> 8);
        final byte loByte = (byte) (word & 0x00FF);

        // set the pointer offset
        systemMemory[physicalAddress] = loByte;
        systemMemory[physicalAddress + 1] = hiByte;
        updateSegmentObservers(physicalAddress, physicalAddress + 1);
    }

    /**
     * Writes a double word (four bytes) to memory at the given segment and offset
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @param value   the given word
     */
    public void setDoubleWord(int segment, int offset, int value) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // determine high and low bytes of the word
        final int hiWord = ((value & 0xFFFF0000) >> 16);
        final int loWord = (value & 0x0000FFFF);

        // place the words into memory
        setWord(segment, offset, loWord);
        setWord(segment, offset + 2, hiWord);
        updateSegmentObservers(physicalAddress, physicalAddress + 4);
    }

    /**
     * Fills the contents of memory with the given filler
     */
    public void fill(final int segment, final int offset, final int length, final byte filler) {
        // calculate the starting index
        final int fromIndex = computePhysicalAddress(segment, offset);

        // calculate the ending index
        final int toIndex = fromIndex + length;

        // fill memory from the start to end indices
        Arrays.fill(systemMemory, fromIndex, toIndex, filler);
        updateSegmentObservers(fromIndex, toIndex);
    }

    /**
     * Performs a bitwise AND on the given located at
     * the given segment and offset in memory.
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @param mask    the given bit mask
     */
    public void andByte(int segment, int offset, int mask) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // AND the byte in memory
        systemMemory[physicalAddress] &= (byte) mask;
        updateSegmentObservers(physicalAddress, physicalAddress + 1);
    }

    /**
     * Performs a bitwise OR on the given located at
     * the given segment and offset in memory.
     * @param segment the segment of the memory location
     * @param offset  the offset of the memory location
     * @param mask    the given bit mask
     */
    public void orByte(int segment, int offset, int mask) {
        // compute the physical address
        final int physicalAddress = computePhysicalAddress(segment, offset);

        // AND the byte in memory
        systemMemory[physicalAddress] |= (byte) mask;
        updateSegmentObservers(physicalAddress, physicalAddress + 1);
    }

    /**
     * Retrieves all available bytes from the given stream
     * @param is the given {@link InputStream input stream}
     * @return all available bytes from the given stream
     * @throws IOException
     */
    protected byte[] getBytes(InputStream is) throws IOException {
        // create a byte containing
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

        // create a read buffer
        final byte[] buf = new byte[1024];

        // read the bytes into memory
        int count;
        while ((count = is.read()) != -1) {
            baos.write(buf, 0, count);
        }

        // returns the bytes
        return baos.toByteArray();
    }

    private void updateSegmentObservers(final int startAddress, final int endAddress) {
        registeredSegments.stream()
                .filter(rs -> startAddress >= rs.startAddress && endAddress <= rs.endAddress)
                .forEach(rs -> rs.listener.segmentUpdated(startAddress, endAddress));
    }

    /**
     * Represents a registered segment
     */
    private static class RegisteredSegment {
        private MemorySegmentListener listener;
        private final int startAddress;
        private final int endAddress;

        public RegisteredSegment(int startAddress, int endAddress, MemorySegmentListener listener) {
            this.listener = listener;
            this.startAddress = startAddress;
            this.endAddress = endAddress;
        }
    }

    /**
     * Represents a memory segment observer
     */
    public static interface MemorySegmentListener {

        void segmentUpdated(int fromAddress, int toAddress);

    }

}