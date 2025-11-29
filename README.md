# SupremeASM

A assembly/machine code like language made by jwmo in Java for learning purposes and also why not? Also I am bad at naming things so I just picked the first word I thought of.


Development in progress.


## Language Rules

Instructions are 4/8 bytes and written in hex. However, print instructions will print in base 10.


 Anything in memory is stored as ints (4 bytes). One memory slot is equal to 4 bytes. Memory is allocated by the compiler implicitly for instructions. Memory should be explicitly allocated by instructions to store any data, and deallocated if needed. (However, there is nothing stopping one from hardcoding a memory address to use without allocating it)
 
 
 Memory is allocated as blocks, and can only be deallocated block by block. 


Like Java, ints are signed values.


PC holds the next instruction to execute, not the current instruction being executed.


r0 recieves return values for all instructions. Most instructions return either 1 for success or -1 for fail. Special cases:
- memory allocation instruction returns address of first memory slot in the allocated memory block
 - memory defragment instruction returns number of blocks defragmented (coalesced together).
- halt returns 0, which tells the CPU to end execution

## ISA:

| operation | semantics | machine code |
|---|---|---|
| load int v into register r | v -> r[r] | 0ree----vvvvvvvv |
| load base + offset | m[r[r] + o] -> r[s] | 00ros--- |
| load indexed | m[r[r] + r[o]] -> r[s] | 01ros--- |
| store base + offset | r[r] -> m[r[s] + o] | 10rso--- |
| store indexed | r[r] -> m[r[s] + r[o]] | 11rso--- |
| copy register value into another register | r[r] -> r[s] | 20rs---- |
| increment a register value | r[r] + 1 -> r[r] | 210r---- |
| decrement a register value | r[r] - 1 -> r[r] | 220r---- |
| add two register values | r[r] + r[s] -> r[s] | 23rs---- |
| not a register value | ~r[r] -> r[r] | 240r---- |
| and two register values | r[r] & r[s] -> r[s] | 25rs---- |
| indirect jump to another instruction (o is a signed 2 byte value) | pc + o -> pc | a00-oooo |
| if a register value is equal to zero, then indirect jump to another instruction (o is a signed 2 byte value) | if r[r] == 0: pc + o -> pc | a1r-oooo |
| if a register value is greater than another, then indirect jump to another instruction (o is a signed 2 byte value) | if r[r] > r[s]: pc + o -> pc | a2rsoooo |
| direct jump to another instruction | v -> pc | afee----vvvvvvvv |
| print register value in terminal | print(r[r]) | e00r---- |
| print memory value with base + offset in terminal | print(m[r[r] + o]) | e0ro---- |
| print register value in terminal with ascii conversion | printWithFormatting(r[r]) | e10r---- |
| print memory value with base + offset in terminal with ascii conversion | printWithFormatting(m[r[r] + o]) | e1ro---- |
| allocate a memory block with x memory slots (x*4 bytes) | malloc(x*4) | f1ee----xxxxxxxx |
| deallocate the memory block with address stored in register | free(r[r]) | f20r---- | 
| defragment memory (tries to coalesce all memory blocks by iterating over the entire memory until all possible blocks are coalesced. this may take a while if memory is too fragmented) | defrag() | f3------ |
| do nothing | nop | f0------ |
| print all register values in terminal | dumpCPU() | fd------ |
| print all non-zero memory values in terminal | dumpMem() | fe------ |
| halt | halt | ffffffff |


## Examples:

### Print "Hello World!":
f1eeffff0000000d 2009ffff 01eeffff00000048 02eeffff00000065 03eeffff0000006c 04eeffff0000006f 05eeffff00000020 06eeffff00000077 07eeffff00000072 08eeffff00000064 10190fff 10291fff 10392fff 10393fff 10494fff 10595fff 10696fff 10497fff 10798fff 10399fff 1089afff 01eeffff00000021 1019bfff 01eeffff0000000a 1019cfff e190ffff e191ffff e192ffff e193ffff e194ffff e195ffff e196ffff e197ffff e198ffff e199ffff e19affff e19bffff e19cffff ffffffff

### Generate, print, and sum numbers 1-10 using a loop:
01eeffff00000001 04eeffff0000000a 02eeffff0000000b 2402ffff 2102ffff 2312ffff a12f0005 e001ffff e104ffff 2313ffff 2101ffff a00ffff5 e003ffff ffffffff
