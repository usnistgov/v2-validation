<?xml version="1.0" encoding="UTF-8"?>
<!-- Schematron written for conformance statements according to the Prescriber/Pharmacist Interface Script Standard -->
<!-- These rules are defined for Meaningful Use -->

<!--schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"-->
<schema xmlns="http://www.ascc.net/xml/schematron">

    <title> Message validation for SCRIPT 10.6 Test name: NIST_CEHRT_Change_Scenario_1_TestStep0
        Updated: 2016-01-05 </title>

    <ns prefix="script" uri="http://www.ncpdp.org/schema/SCRIPT"/>

    <phase id="errors">
        <active pattern="Errors"/>
    </phase>

    <phase id="warnings">
        <active pattern="Warnings"/>
    </phase>

    <!--  -->

    <pattern id="Warnings">
        <rule
            context="/script:Message/script:Body/script:*/script:Pharmacy/script:Identification">
            <assert test="script:NCPDPID != ''"> WARNING: The element
                Pharmacy/Identification/NCPDPID is not present. Use of this element is recommended. </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:*/script:MedicationPrescribed">
            <assert
                test="
                not(script:DrugCoded/script:ProductCodeQualifier = 'ND')
                or
                string-length(script:DrugCoded/script:ProductCode) = 11"
                > WARNING: If DrugCoded/ProductCode/Qualifier is 'ND", then
                DrugCoded/ProductCode should contain an 22-digit NDC. 
            </assert>
            
            <!-- TODO: Confirm meaning of values 7 and 8. -->
            <assert
                test="
                    not(script:Substitutions)
                    or
                    ((script:Substitutions = '0')
                    or (script:Substitutions = '1')
                    or (script:Substitutions = '7')
                    or (script:Substitutions = '8'))"
                > WARNING: If Substitutions is present, its value should be 0 or 1 or 7 or 8. 
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:*/script:MedicationDispensed">
            <assert
                test="
                not(script:DrugCoded/script:ProductCodeQualifier = 'ND')
                or
                string-length(script:DrugCoded/script:ProductCode) = 11"
                > WARNING: If DrugCoded/ProductCode/Qualifier is 'ND", then
                DrugCoded/ProductCode should contain an 11-digit NDC. 
            </assert>
            
            <!-- TODO: Confirm meaning of values 7 and 8. -->
            <assert
                test="
                not(script:Substitutions)
                or
                ((script:Substitutions = '0')
                or (script:Substitutions = '1')
                or (script:Substitutions = '7')
                or (script:Substitutions = '8'))"
                > WARNING: If Substitutions is present, its value should be 0 or 1 or 7 or 8. 
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:*/script:MedicationRequested">
            <assert
                test="
                not(script:DrugCoded/script:ProductCodeQualifier = 'ND')
                or
                string-length(script:DrugCoded/script:ProductCode) = 11"
                > WARNING: If DrugCoded/ProductCode/Qualifier is 'ND", then
                DrugCoded/ProductCode should contain an 11-digit NDC. 
            </assert>
            
            <!-- TODO: Confirm meaning of values 7 and 8. -->
            <assert
                test="
                not(script:Substitutions)
                or
                ((script:Substitutions = '0')
                or (script:Substitutions = '1')
                or (script:Substitutions = '7')
                or (script:Substitutions = '8'))"
                > WARNING: If Substitutions is present, its value should be 0 or 1 or 7 or 8. 
            </assert>
        </rule>
        
    </pattern>

    <!--  -->
    <pattern id="Errors">


        <!--  Context-free validations not handled by the XML schema  -->

        <!--  Modifications made on 20160106 by FM:  -->
        <!-- Removed the version number tests, because they are covered by the schema validation function in the validator -->
        <!-- Removed the to/from presence and to/from qualifier value tests, because they are covered by the schema validation function in the validator -->
        <!-- Removed the message id presence test, because they are covered by the schema validation function in the validator -->
        <!-- Removed the prescriber and pharmacy identification and andatory name, address and phone number tests, because they are covered by the schema validation function in the validator -->
        <!-- Removed the patient last name, first name and gender tests, because they are covered by the schema validation function in the validator -->
        <!-- Removed the medication name, quantity value/qualifier, directions and refills presence tests, because they are covered by the schema validation function in the validator -->
        <!-- Removed the version number tests, because they are covered by the schema validation function in the validator -->

        <!-- Tests that apply to MedicationPrescribed in all message types -->
        <rule context="/script:Message/script:Body/script:*/script:MedicationPrescribed">
            <assert
                test="
                    not(script:DrugCoded/script:ProductCode)
                    or
                    script:DrugCoded/script:ProductCodeQualifier != ''"
                > ERROR: If DrugCoded/ProductCode is present, then
                DrugCoded/ProductCodeQualifier must also be present. 
            </assert>
            <assert
                test="
                    not(script:DrugCoded/script:DrugDBCode)
                    or
                    script:DrugCoded/script:DrugDBCodeQualifier != ''"
                > ERROR: If DrugCoded/DrugDBCode is present, then
                DrugCoded/DrugDBCodeQualifier must also be present. 
            </assert>
            <assert
                test="
                    not(script:DrugCoded/script:FormCode)
                    or
                    script:DrugCoded/script:FormSourceCode"
                > ERROR: If DrugCoded/FormCode is present, then
                DrugCoded/FormSourceCode must also be present. 
            </assert>
            <assert
                test="
                    not(script:DrugCoded/script:FormSourceCode)
                    or
                    script:DrugCoded/script:FormSourceCode = 'AA'"
                > ERROR: If DrugCoded/FormSourceCode is present, its value
                should be AA. 
            </assert>
            <assert
                test="
                    not(script:DrugCoded/script:StrengthCode)
                    or
                    script:DrugCoded/script:StrengthSourceCode"
                > ERROR: If DrugCoded/StrengthCode is present, then
                DrugCoded/StrengthSourceCode must also be present. 
            </assert>
            <assert
                test="
                    not(script:DrugCoded/script:StrengthSourceCode)
                    or
                    script:DrugCoded/script:StrengthSourceCode = 'AB'"
                > ERROR: If DrugCoded/StrengthSourceCode is present, its value
                should be AB. 
            </assert>
            <assert
                test="
                    not(script:DrugCoded/script:DEASchedule)
                    or
                    (script:DrugCoded/script:DEASchedule = 'C48672')
                    or (script:DrugCoded/script:DEASchedule = 'C48675')
                    or (script:DrugCoded/script:DEASchedule = 'C48676')
                    or (script:DrugCoded/script:DEASchedule = 'C48677')
                    or (script:DrugCoded/script:DEASchedule = 'C48679')
                    or (script:DrugCoded/script:DEASchedule = 'C38046')"
                > ERROR: If DrugCoded/DEASchedule is present, its value should
                be C48672 or C48675 or C48676 or C48677 or C48679 or C38046. </assert>
            <!-- TODO: Check whether the next rule is needed in any message type. The schema covers the mandatory-ness in NexRx, but not sure about other message types  
            <assert test="not(script:Quantity/script:PotencyUnitCode)
                or
                script:Quantity/script:UnitSourceCode">
                ERROR: If Quantity/PotencyUnitCode is present, then Quantity/UnitSourceCode must also be present.
            </assert>
            -->
            <assert
                test="
                    not(script:Quantity/script:UnitSourceCode)
                    or
                    script:Quantity/script:UnitSourceCode = 'AC'"
                > ERROR: If Quantity/UnitSourceCode is present, its value
                should be AC. 
            </assert>
            <!-- TODO: See if there's IG or Rec Doc support for requiring the diagnosis qualifier (seems like there should be... though is optional in the schema) -->
            <assert test="not(script:Diagnosis/script:Primary/script:Value)
                or
                script:Diagnosis/script:Primary/script:Qualifier">
                ERROR: If Diagnosis/Primary/Value is present, then Diagnosis/Primary/Qualifier must also be present.
            </assert>
            <assert test="not(script:Diagnosis/script:Secondary/script:Value)
                or
                script:Diagnosis/script:Secondary/script:Qualifier">
                ERROR: If Diagnosis/Secondary/Value is present, then Diagnosis/Secondary/Qualifier must also be present.
            </assert>
        </rule>

        <!-- Tests that apply to MedicationDispensed in all message types -->
        <rule context="/script:Message/script:Body/script:*/script:MedicationDispensed">
            <assert
                test="
                not(script:DrugCoded/script:ProductCode)
                or
                script:DrugCoded/script:ProductCodeQualifier != ''"
                > ERROR: If DrugCoded/ProductCode is present, then
                DrugCoded/ProductCodeQualifier must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:DrugDBCode)
                or
                script:DrugCoded/script:DrugDBCodeQualifier != ''"
                > ERROR: If DrugCoded/DrugDBCode is present, then
                DrugCoded/DrugDBCodeQualifier must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:FormCode)
                or
                script:DrugCoded/script:FormSourceCode"
                > ERROR: If DrugCoded/FormCode is present, then
                DrugCoded/FormSourceCode must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:FormSourceCode)
                or
                script:DrugCoded/script:FormSourceCode = 'AA'"
                > ERROR: If DrugCoded/FormSourceCode is present, its value
                should be AA. </assert>
            <assert
                test="
                not(script:DrugCoded/script:StrengthCode)
                or
                script:DrugCoded/script:StrengthSourceCode"
                > ERROR: If DrugCoded/StrengthCode is present, then
                DrugCoded/StrengthSourceCode must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:StrengthSourceCode)
                or
                script:DrugCoded/script:StrengthSourceCode = 'AB'"
                > ERROR: If DrugCoded/StrengthSourceCode is present, its value
                should be AB. </assert>
            <assert
                test="
                not(script:DrugCoded/script:DEASchedule)
                or
                (script:DrugCoded/script:DEASchedule = 'C48672')
                or (script:DrugCoded/script:DEASchedule = 'C48675')
                or (script:DrugCoded/script:DEASchedule = 'C48676')
                or (script:DrugCoded/script:DEASchedule = 'C48677')
                or (script:DrugCoded/script:DEASchedule = 'C48679')
                or (script:DrugCoded/script:DEASchedule = 'C38046')"
                > ERROR: If DrugCoded/DEASchedule is present, its value should
                be C48672 or C48675 or C48676 or C48677 or C48679 or C38046. </assert>
            <!-- TODO: Check whether the next rule is needed in any message type. The schema covers the mandatory-ness in NexRx, but not sure about other message types  
            <assert test="not(script:Quantity/script:PotencyUnitCode)
                or
                script:Quantity/script:UnitSourceCode">
                ERROR: If Quantity/PotencyUnitCode is present, then Quantity/UnitSourceCode must also be present.
            </assert>
            -->
            <assert
                test="
                not(script:Quantity/script:UnitSourceCode)
                or
                script:Quantity/script:UnitSourceCode = 'AC'"
                > ERROR: If Quantity/UnitSourceCode is present, its value
                should be AC. 
            </assert>
            <!-- TODO: See if there's IG or Rec Doc support for requiring the diagnosis qualifier (seems like there should be... though is optional in the schema) -->
            <assert test="not(script:Diagnosis/script:Primary/script:Value)
                or
                script:Diagnosis/script:Primary/script:Qualifier">
                ERROR: If Diagnosis/Primary/Value is present, then Diagnosis/Primary/Qualifier must also be present.
            </assert>
            <assert test="not(script:Diagnosis/script:Secondary/script:Value)
                or
                script:Diagnosis/script:Secondary/script:Qualifier">
                ERROR: If Diagnosis/Secondary/Value is present, then Diagnosis/Secondary/Qualifier must also be present.
            </assert>
        </rule>
        
        <!-- Tests that apply to MedicationRequested in all message types -->
        <rule context="/script:Message/script:Body/script:*/script:MedicationRequested">
            <assert
                test="
                not(script:DrugCoded/script:ProductCode)
                or
                script:DrugCoded/script:ProductCodeQualifier != ''"
                > ERROR: If DrugCoded/ProductCode is present, then
                DrugCoded/ProductCodeQualifier must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:DrugDBCode)
                or
                script:DrugCoded/script:DrugDBCodeQualifier != ''"
                > ERROR: If DrugCoded/DrugDBCode is present, then
                DrugCoded/DrugDBCodeQualifier must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:FormCode)
                or
                script:DrugCoded/script:FormSourceCode"
                > ERROR: If DrugCoded/FormCode is present, then
                DrugCoded/FormSourceCode must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:FormSourceCode)
                or
                script:DrugCoded/script:FormSourceCode = 'AA'"
                > ERROR: If DrugCoded/FormSourceCode is present, its value
                should be AA. </assert>
            <assert
                test="
                not(script:DrugCoded/script:StrengthCode)
                or
                script:DrugCoded/script:StrengthSourceCode"
                > ERROR: If DrugCoded/StrengthCode is present, then
                DrugCoded/StrengthSourceCode must also be present. </assert>
            <assert
                test="
                not(script:DrugCoded/script:StrengthSourceCode)
                or
                script:DrugCoded/script:StrengthSourceCode = 'AB'"
                > ERROR: If DrugCoded/StrengthSourceCode is present, its value
                should be AB. </assert>
            <assert
                test="
                not(script:DrugCoded/script:DEASchedule)
                or
                (script:DrugCoded/script:DEASchedule = 'C48672')
                or (script:DrugCoded/script:DEASchedule = 'C48675')
                or (script:DrugCoded/script:DEASchedule = 'C48676')
                or (script:DrugCoded/script:DEASchedule = 'C48677')
                or (script:DrugCoded/script:DEASchedule = 'C48679')
                or (script:DrugCoded/script:DEASchedule = 'C38046')"
                > ERROR: If DrugCoded/DEASchedule is present, its value should
                be C48672 or C48675 or C48676 or C48677 or C48679 or C38046. </assert>
            <!-- TODO: Check whether the next rule is needed in any message type. The schema covers the mandatory-ness in NexRx, but not sure about other message types  
            <assert test="not(script:Quantity/script:PotencyUnitCode)
                or
                script:Quantity/script:UnitSourceCode">
                ERROR: If Quantity/PotencyUnitCode is present, then Quantity/UnitSourceCode must also be present.
            </assert>
            -->
            <assert
                test="
                not(script:Quantity/script:UnitSourceCode)
                or
                script:Quantity/script:UnitSourceCode = 'AC'"
                > ERROR: If Quantity/UnitSourceCode is present, its value
                should be AC. 
            </assert>
            <!-- TODO: See if there's IG or Rec Doc support for requiring the diagnosis qualifier (seems like there should be... though is optional in the schema) -->
            <assert test="not(script:Diagnosis/script:Primary/script:Value)
                or
                script:Diagnosis/script:Primary/script:Qualifier">
                ERROR: If Diagnosis/Primary/Value is present, then Diagnosis/Primary/Qualifier must also be present.
            </assert>
            <assert test="not(script:Diagnosis/script:Secondary/script:Value)
                or
                script:Diagnosis/script:Secondary/script:Qualifier">
                ERROR: If Diagnosis/Secondary/Value is present, then Diagnosis/Secondary/Qualifier must also be present.
            </assert>
        </rule>
        
        

        <!-- NewRx-specific tests -->
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed">
            <assert
                test="
                not(script:Quantity/script:CodeListQualifier)
                or
                script:Quantity/script:CodeListQualifier = '38'"
                > ERROR: If Quantity/CodeListQualifier is present, its value should be 38. </assert>
        </rule>
    </pattern>


</schema>
