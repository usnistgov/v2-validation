<?xml version="1.0" encoding="UTF-8"?>
<!-- Schematron written for conformance statements according to the Prescriber/Pharmacist Interface Script Standard -->
<!-- These rules are defined for Meaningful Use -->

<!--schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"-->
<schema xmlns="http://www.ascc.net/xml/schematron">

    <title>
        Message validation for SCRIPT 10.6
        Test name: NIST_CEHRT_Change_Scenario_1_TestStep0
        Updated: 2016-01-07
    </title>

    <ns prefix="script" uri="http://www.ncpdp.org/schema/SCRIPT"/>

    <phase id="errors">
        <active pattern="Errors"></active>
    </phase>

    <phase id="warnings">
        <active pattern="Warnings"></active>
    </phase>

    <!--  -->

    <pattern id="Warnings">
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Identification">
            <assert test="script:NCPDPID='1629900'">
            	WARNING: The test case value '1629900' is not present in the conditional element /Message/Body/NewRx/Pharmacy/Identification/NCPDPID.
            	Support of this element is recommended.
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Identification">
            <assert test="script:NPI='3030000003'">
            	WARNING: The test case value '3030000003' is not present in the conditional element /Message/Body/NewRx/Pharmacy/Identification/NPI.
            	Support of this element is recommended.
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Identification">
            <assert test="script:DEANumber='FF1234567'">
            	WARNING: The test case value 'FF1234567' is not present in the conditional element /Message/Body/NewRx/Prescriber/Identification/DEANumber.
            	Support of this element is recommended.
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Diagnosis/script:Primary">
            <assert test="script:Qualifier='ABF'">
            	WARNING: The test case value 'ABF' is not present in the conditional element /Message/Body/NewRx/MedicationPrescribed/Diagnosis/Primary/Qualifier.
            	Support of this element is recommended.
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Diagnosis/script:Primary">
            <assert test="script:Value='I201'">
            	WARNING: The test case value 'I201' is not present in the conditional element /Message/Body/NewRx/MedicationPrescribed/Diagnosis/Primary/Value.
            	Support of this element is recommended.
            </assert>
        </rule>
    </pattern>

    <!--  -->
    <pattern id="Errors">
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Identification">
            <assert test="not(script:NCPDPID)
            	or script:NCPDPID='' 
            	or script:NCPDPID='1629900'">
            	ERROR: If populated, the value of /Message/Body/NewRx/Pharmacy/Identification/NCPDPID shall be set to '1629900'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Identification">
            <assert test="not(script:NPI)
            	or script:NPI='' 
            	or script:NPI='3030000003'">
            	ERROR: If populated, the value of /Message/Body/NewRx/Pharmacy/Identification/NPI shall be set to '3030000003'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:StoreName">
            <assert test="lower-case(.)='mail order pharmacy 10.6mu nocs'">
            	ERROR: The value of /Message/Body/NewRx/Pharmacy/StoreName shall be set to 'Mail Order Pharmacy 10.6MU NOCS' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Address/script:AddressLine1">
            <assert test="lower-case(.)='1629-90 supply ln'">
            	ERROR: The value of /Message/Body/NewRx/Pharmacy/Address/AddressLine1 shall be set to '1629-90 Supply Ln' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Address/script:City">
            <assert test="lower-case(.)='saint louis'">
            	ERROR: The value of /Message/Body/NewRx/Pharmacy/Address/City shall be set to 'Saint Louis' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Address/script:State">
            <assert test=".='MO'">
            	ERROR: The value of /Message/Body/NewRx/Pharmacy/Address/State shall be set to 'MO'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Pharmacy/script:Address/script:ZipCode">
            <assert test=".='63105'">
            	ERROR: The value of /Message/Body/NewRx/Pharmacy/Address/ZipCode shall be set to '63105'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Identification">
            <assert test="not(script:DEANumber)
            	or script:DEANumber='' 
            	or script:DEANumber='FF1234567'">
            	ERROR: If populated, the value of /Message/Body/NewRx/Prescriber/Identification/DEANumber shall be set to 'FF1234567'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Identification/script:NPI">
            <assert test=".='1619967999'">
            	ERROR: The value of /Message/Body/NewRx/Prescriber/Identification/NPI shall be set to '1619967999'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Name/script:LastName">
            <assert test="lower-case(.)='macclare'">
            	ERROR: The value of /Message/Body/NewRx/Prescriber/Name/LastName shall be set to 'MacClare' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Name/script:FirstName">
            <assert test="lower-case(.)='susan'">
            	ERROR: The value of /Message/Body/NewRx/Prescriber/Name/FirstName shall be set to 'Susan' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Address/script:AddressLine1">
            <assert test="lower-case(.)='10105 trailblazer ct'">
            	ERROR: The value of /Message/Body/NewRx/Prescriber/Address/AddressLine1 shall be set to '10105 Trailblazer Ct' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Address/script:City">
            <assert test="lower-case(.)='portland'">
            	ERROR: The value of /Message/Body/NewRx/Prescriber/Address/City shall be set to 'Portland' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Address/script:State">
            <assert test=".='OR'">
            	ERROR: The value of /Message/Body/NewRx/Prescriber/Address/State shall be set to 'OR'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Prescriber/script:Address/script:ZipCode">
            <assert test=".='97215'">
            	ERROR: The value of /Message/Body/NewRx/Prescriber/Address/ZipCode shall be set to '97215'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Patient/script:Name/script:LastName">
            <assert test="lower-case(.)='biscayne'">
            	ERROR: The value of /Message/Body/NewRx/Patient/Name/LastName shall be set to 'Biscayne' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Patient/script:Name/script:FirstName">
            <assert test="lower-case(.)='sophia'">
            	ERROR: The value of /Message/Body/NewRx/Patient/Name/FirstName shall be set to 'Sophia' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Patient/script:Gender">
            <assert test=".='F'">
            	ERROR: The value of /Message/Body/NewRx/Patient/Gender shall be set to 'F'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Patient/script:Address/script:AddressLine1">
            <assert test="lower-case(.)='991 monroe avenue'">
            	ERROR: The value of /Message/Body/NewRx/Patient/Address/AddressLine1 shall be set to '991 Monroe Avenue' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Patient/script:Address/script:City">
            <assert test="lower-case(.)='port charlotte'">
            	ERROR: The value of /Message/Body/NewRx/Patient/Address/City shall be set to 'Port Charlotte' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Patient/script:Address/script:State">
            <assert test=".='FL'">
            	ERROR: The value of /Message/Body/NewRx/Patient/Address/State shall be set to 'FL'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:Patient/script:Address/script:ZipCode">
            <assert test=".='33952'">
            	ERROR: The value of /Message/Body/NewRx/Patient/Address/ZipCode shall be set to '33952'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:DrugCoded/script:DrugDBCode">
            <assert test=".='207772'">
            	ERROR: The value of /Message/Body/NewRx/MedicationPrescribed/DrugCoded/DrugDBCode shall be set to '207772'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:DrugCoded/script:DrugDBCodeQualifier">
            <assert test=".='SBD'">
            	ERROR: The value of /Message/Body/NewRx/MedicationPrescribed/DrugCoded/DrugDBCodeQualifier shall be set to 'SBD'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Quantity/script:Value">
            <assert test=".='53'">
            	ERROR: The value of /Message/Body/NewRx/MedicationPrescribed/Quantity/Value shall be set to '53'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Quantity/script:PotencyUnitCode">
            <assert test=".='C48542'">
            	ERROR: The value of /Message/Body/NewRx/MedicationPrescribed/Quantity/PotencyUnitCode shall be set to 'C48542'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Refills/script:Value">
            <assert test=".='0'">
            	ERROR: The value of /Message/Body/NewRx/MedicationPrescribed/Refills/Value shall be set to '0'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Substitutions">
            <assert test=".='1'">
            	ERROR: The value of /Message/Body/NewRx/MedicationPrescribed/Substitutions shall be set to '1'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Diagnosis/script:ClinicalInformationQualifier">
            <assert test="lower-case(.)='1'">
            	ERROR: The value of /Message/Body/NewRx/MedicationPrescribed/Diagnosis/ClinicalInformationQualifier shall be set to '1' (case insensitive). (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Diagnosis/script:Primary">
            <assert test="not(script:Qualifier)
            	or script:Qualifier='' 
            	or script:Qualifier='ABF'">
            	ERROR: If populated, the value of /Message/Body/NewRx/MedicationPrescribed/Diagnosis/Primary/Qualifier shall be set to 'ABF'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
        <rule context="/script:Message/script:Body/script:NewRx/script:MedicationPrescribed/script:Diagnosis/script:Primary">
            <assert test="not(script:Value)
            	or script:Value='' 
            	or script:Value='I201'">
            	ERROR: If populated, the value of /Message/Body/NewRx/MedicationPrescribed/Diagnosis/Primary/Value shall be set to 'I201'. (Current value: '<value-of select="."/>')
            </assert>
        </rule>
    </pattern>


</schema>
