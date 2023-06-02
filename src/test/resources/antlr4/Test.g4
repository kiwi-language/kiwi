
grammar Test;
expr: expr '?' expr ':' expr
    | INT
    ;
INT: [0-9]+;
WS: (' ' | '\t')+ -> channel(HIDDEN);