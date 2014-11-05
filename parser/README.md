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