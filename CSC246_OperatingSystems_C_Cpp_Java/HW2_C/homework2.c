#include <stdio.h>

int global = 5;

void foo();

main() {

    int id = -2;
    printf("main(): global = %d\n" , global) ;
    foo();
    foo();

    id = fork();

    if (id != 0) {
       global++;
       wait(NULL);
    }

   global++;
   printf( "main(): global = %d\n" , global) ;

}

void foo() {

    static int staticInt = 3;
    int localInt = 1;
    printf("foo(): staticInt = %d \n" , staticInt) ;
    printf("foo(): localInt = %d \n" , localInt) ;
    staticInt++;
    localInt++;

}
