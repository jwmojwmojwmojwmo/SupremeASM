package main;
// Represents all 64kb of memory in the system
// The MainMemory class handles all mainmemory methods and assigns memory to the cpu accordingly
// MainMemory will act as a heap in this implementation
// MainMemory stores data as 4 byte "slots"
// Memory blocks are stored with an 4 byte header and footer, taking up two "slots" of memory. The first details the size of the payload, the second details if the block is free
public class MainMemory {
    private static MainMemory mainMemory;
    private final int[] memory;
    private final int maxMemory = 16384;

    private MainMemory() {
        memory = new int[maxMemory];
        memory[0] = maxMemory - 2;
        memory[maxMemory - 1] = 0; // 0 means not in use, or free
    }

    public static MainMemory getInstance() {
        if (mainMemory == null) {
            mainMemory = new MainMemory();
        }
        return mainMemory;
    }

    // allows devices to request memory, returning the address of the first block of
    // payload
    public int requestMemoryBlock(int size) {
        int i = 0; // i is the address of the header
        while (memory[i] < (size + 2) || memory[getFooter(i)] == 1) {
            i += memory[i] + 2; // increment i by size of payload + size of header + size of footer
            if (i >= memory.length) {
                return -1; // memory overflow
            }
        }
        allocateBlock(i, size);
        return i + 1;
    }

    // allows devices to tell MainMemory to free a memory block, given the address of its payload
    public void freeMemoryBlock(int payload) {
        payload--; // set address to header
        if (memory[getFooter(payload)] == 0) {
            return;
        }
        deallocateBlock(payload);
    }

    // tries to coalesce every block in memory, returning number of blocks defragmented
    public int defragmentMemory() {
        int i;
        int blockCounter; // i is the address of the header
        int totalBlockCounter = 0; // number of defragmented blocks
        int footer;
        do {
            i = 0;
            blockCounter = 0;
            while (i < memory.length && getFooter(i) < maxMemory - 1) {
                footer = getFooter(i);
                if (memory[footer] == 0 && memory[getFooter(footer + 1)] == 0) {
                    coalesce(i, footer + 1);
                    blockCounter += 2;
                }
                i = getFooter(i) + 1;
            }
            totalBlockCounter += blockCounter;
        } while (blockCounter != 0);
        return totalBlockCounter;
    }

    public int read(int address) {
        return memory[address];
    }

    public void write(int address, int value) {
        memory[address] = value;
    }

    public int dump() {
        System.out.println("\nMemory:");
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                System.out.println(i + ": " + memory[i]);
            }
        }
        return 1;
    }

    // given the address of the header of a free memory block, break that block into
    // an allocated block of size size and unallocated block with the remainder
    private void allocateBlock(int header, int size) {
        int restSize = memory[header] - size - 2;
        memory[header] = size;
        memory[getFooter(header)] = 1;
        memory[getFooter(header) + 1] = restSize;
    }

    // given the address of the header of a allocated memory block, make that block free and attempt to join it with the next neighbouring block
    private void deallocateBlock(int header) {
        int footer = getFooter(header);
        memory[footer] = 0;
        if (footer >= maxMemory - 1) return;
        if (memory[getFooter(footer + 1)] == 0) {
            coalesce(header, footer + 1);
        }
    }

    // joins two free blocks together
    private void coalesce(int firstHeader, int secondHeader) {
        int size = memory[firstHeader] + memory[secondHeader] + 2;
        memory[secondHeader] = 0;
        memory[firstHeader] = size;
    }

    // returns address of the footer of that block given the address of its header
    private int getFooter(int header) {
        return header + memory[header] + 1;
    }
}