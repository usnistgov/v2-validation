# HL7 Instance model

## HL7 Null value

    *HL7 does not care how systems actually store data within an application. When fields are transmitted, 
    they are sent as character strings. Except where noted, HL7 data fields may take on the null value. 
    Sending the null value, which is transmitted as two double quote marks (""), is different from omitting 
    an optional data field. The difference appears when the contents of a message will be used to update a 
    record in a database rather than create a new one. If no value is sent, (i.e., it is omitted) the old 
    value should remain unchanged. If the null value is sent, the old value should be changed to null. 
    For further details, see Section 2.6, "Message construction rules".*
    
    ### Null value implementation
        - Null value is represented as "" (Note that " " is not considered as Null)
        - Null value apply to data element (field or component)
        - When the data element is complex : 
            ""    <=> all children are Null for example in case of an HD field "" <=> ""^""^""
            xx^"" <=> HD.1 = "xx", HD.2 is Null and HD.3 is missing
            ""^xx <=> HD.1 is Null, HD.2 = xx and HD.3 is missing
    
    ### Null value and validation
    

The above is little obscure.

Here is what I think. I believe that the data type of the file should be taken into account.
So for primitive field there is no problem the will be "" and will keep its semantic.

For complex and depending on the data type Null ("") means every primitive component/sub component should be null.
"" <=> ""^""^""^""^""^ <=> ""^""&""&""^""   ( <=> means equivalent)
In term of validation

Usage:
     "" and R => OK
      "" and X or W => Error or Warning

Cardinality

""~""....

## Escape Sequence
        
    ### 
        
        \F\     field separator
        \S\     component separator
        \T\     subcomponent separator
        \R\     repetition separator
        \E\     escape character
        \P\     truncation character

        \H\     start highlighting
        \N\     normal text (end highlighting)
        
        \Xdddd...\   hexadecimal data
        \Zdddd...\   locally defined escape sequence
        
        
    ### Character Sets
        
        \Cxxyy\     single-byte character set       Examples: \C2842\ => ASCII  \C2D41\ => Latin Alphabet 1
        \Mxxyyzz\   multi-byte character set        Examples: \M2442\ ISO-IR87 (JIS X 0208 : Kanji, hiragana and katakana)
    
    
    ### Formatting commands (Only allowed in FT data type)
        
        .sp     <number> End current output line and skip <number> vertical spaces.
        .br     Begin new output line.
        .fi     Begin word wrap or fill mode.
        .nf     Begin no-wrap mode.
        .in     <number> Indent <number> of spaces,
        .ti     <number> Temporarily indent <number> of spaces
        .sk     < number> Skip <number> spaces to the right.
        .ce     End current output line and center the next line.
    
    According to HL7 v28:
        *For the purposes of determining length, all the characters inside the escape (all between the opening and
        closing \, not including the \ symbols themselves) count towards the length. This applies to all the escape
        sequences, including the formatting ones described below.*
     
   
   Questions:
   
        1) Is there any use case for \Zdddd...\ ?
        
        2) How \Cxxyy\ and \Mxxyyzz\ are used ? 
            Are they just flags indicating that the character sets has changed ?
            If yes, then how the length should be computed ? before or after character set resolution ? 
   
   
   Salifou's Comments
   
        1) \H\ and \N\ should be classified as formatting commands. The rational is that contrary to \F\ \S\ they do not have a corresponding replacement ... 
        
        2) Because of the formatting commands, length computation on a TX data type is problematic. 
   
   
   Note that truncation was introduced in HL7 2.7 but the escape sequence is defined in HL7 2.8
   
## Value semantic comparison

Null, Number, Date, Time, DateTime, Text, FormattedText


### Comparing Null with 
    - Null: 