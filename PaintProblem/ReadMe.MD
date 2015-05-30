Problem Summary
====================
The essence of the problem is a constrained combinatorics problem. The paint numbers are in effect index positions for a 
bit array, so that when there is paint #1 which is matte out of a 3-batch selection, the equivalent array is 

1,0,0 - Customer 1 selecting paint #1 matte

Similary, if paint #2 was matte for another customer the two binary array representations for each would be 

1,0,0 - Customer 1 selecting paint #1 matte
0,1,0 - Customer 2 selecting paint #2 matte

Since it is assumed that any unmentioned paint colour selection is automatically set as glossy (0). 

Solution Summary
=====================
Working with our toy example above we have two bit-arrays, with a single column (the last one) have all the 
same value. Therefore this last column must be 0 in the final selection regardless of selectio of the other
two columns. So the search space becomes constrained to find, 

1. A bit-level array which will satisfy both customers
2. A means of finding the combinations of all these potential changes

The search-space for out toy example would therefore be the following bit arrays, with the column position
being the colour number and the value being 1 for matte or 0 for glossy, i.e, 

0,0,0 - Matches neither customer's selection, discarded
0,1,0 - Matches the second customer's selection only.
1,0,0 - Matches the first customer's selection only. 
1,1,0 - Matches both customer's selections, and would be the final result. 

The matching process itself is a logical NOR operation using bitmasks to filter out only those paint selections
related to the customer under test. 
