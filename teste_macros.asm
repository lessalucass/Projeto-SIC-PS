SOMAR   MACRO &A,&B   ; soma dois valores
        LDA &A
        ADD &B

VOLTA   STA &A
        MEND
EXT     MACRO &X
        LDA &X
INT     MACRO &Y,&Z
        STA &Y
        STA &Z
        MEND
        INT  &X,R
        MEND
SOMAR   MACRO &W
        MEND
SEMFIM  MACRO &Q
        LDA &Q