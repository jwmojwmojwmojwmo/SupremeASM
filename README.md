# SupremeASM

A assembly/machine code like language made by jwmo in Java for learning purposes and also why not?


Development in progress.


## Language Rules

Instructions are 4/8 bytes. Anything in memory is stored as ints (4 bytes). One memory slot is equal to 4 bytes. Memory is allocated by the compiler implicitly for instructions. Memory must be explicitly allocated by instructions to store data, and deallocated if needed. Memory is allocated as blocks, and can only be deallocated block by block. 


r0 recieves return values for all instructions. Most instructions return either 1 for success or -1 for fail. Special cases:
- memory allocation instruction returns address of first memory slot in the allocated memory block
 - memory defragment instruction returns number of blocks defragmented (coalesced together).
- halt returns 0, which tells the CPU to end execution

## ISA:

| operation | semantics | machine code |
|---|---|---|
| load int v into register r | v -> r[r] | 0ree----vvvvvvvv |
| load base + offset | m[r[r] + o] -> r[s] | 0ros---- |
| store base + offset | r[r] -> m[r[s] + o] | 1rso---- |
| copy register value into another register | r[r] -> r[s] | 20rs---- |
| print register value in terminal | print(r[r]) | e00r---- |
| print memory value with base + offset in terminal | print(m[r[r] + o]) e0ro---- |
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
f1eeffff0000000d 2009ffff 01eeffff00000048 02eeffff00000065 03eeffff0000006c 04eeffff0000006f 05eeffff00000020 06eeffff00000077 07eeffff00000072 08eeffff00000064 1190ffff 1291ffff 1392ffff 1393ffff 1494ffff 1595ffff 1696ffff 1497ffff 1798ffff 1399ffff 189affff 01eeffff00000021 119bffff 01eeffff0000000a1 19cffff e190ffff e191ffff e192ffff e193ffff e194ffff e195ffff e196ffff e197ffff e198ffff e199ffff e19affff e19bffff e19cffff ffffffff
# SupremeASM
