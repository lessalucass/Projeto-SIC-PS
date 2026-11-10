SOMA2   MACRO   &X,&Y
        LDA     &X
        ADD     &Y
        MEND
SOMA3   MACRO   &P,&Q,&R
        SOMA2   &P,&Q
        ADD     &R
        MEND
        START   0
INICIO  SOMA3   N1,N2,N3
        SOMA2   N1,N1
        END
        