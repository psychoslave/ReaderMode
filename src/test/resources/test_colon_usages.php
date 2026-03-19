<?php

// Label usage
goto label;
label:

// Block start (if, while, for, foreach, case)
if ($x > 0):
    echo 'positive';
endif;

while ($y < 10):
    $y++;
endwhile;

for ($i = 0; $i < 3; $i++):
    echo $i;
endfor;

foreach ([1,2,3] as $v):
    echo $v;
endforeach;

switch ($z) {
    case 1:
        echo 'one';
        break;
    case 2:
        echo 'two';
        break;
}

// Named argument
function foo($bar, $baz) {}
foo(bar: 1, baz: 2);

// Return type
function add(int $a, int $b): int {
    return $a + $b;
}

// Ternary operator
$result = $flag ? 'yes' : 'no';

// Intertwined ternary
$complex = $a ? ($b ? 'x' : 'y') : 'z';

