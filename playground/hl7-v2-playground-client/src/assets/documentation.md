# NIST HL7 v2 Validation Engine Documentation

## Introduction

### What's an HL7 v2 Message

An HL7 v2 message is the atomic unit of data transferred between systems. It is comprised of a group of segments in a defined sequence. Each message has a message type that defines its purpose. 

For example the ADT Message type is used to transmit portions of a patient’s Patient Administration (ADT) data from one system to another. A three-character code contained within each message identifies its type. The real-world event that initiates an exchange of messages is called a trigger event.

**Source: HL7 v2 Standard**

#### HL7 v2 Message Structure

A message is structured in a tree-like data model. A **Message** contains GROUPs and SEGMENTs. A **Group** is a container for Segments and/or other Groups.
A **Segment** contains **__Fields__** which have a **Datatype**. A **Datatype** can either be primitive like a String or Number or Complex and contain **__Components__** which also have a Datatype, when a Component has a complex datatype, its children are called **__SubComponents__** in the context of the message.

- Resources :
  - Segment, Datatype, are reusable blocs that describe data that fits together and can be assigned to a position in the message.
- Data Elements :
  - Fields, Components, SubComponents, are elements within a message the contain data and have a specific structure.

#### How to describe an HL7 v2 Message's Structure

In order to perform any type of parsing / analysis / validation on a message. It is necessary to be able to formally describe the structure of an HL7 v2 message in machine computable format. This format should define what Groups / Segments are expected at what positions of the message as well as their structure and other information regarding their usage and cardinality.

This format is what we call a Message **Profile**. As an analogy, this format can be thought of as the equivalent of a XSD Schema for an XML Document.

## Validation Concepts

## Validation Resources

### Profile

### Value Set Library

### Additional Resources

## Validation Engine

### Message Parser

### Paths in the Message

### Validation Modules

### Structure Validation
### Content Validation
### Vocabulary Validation
### Co-Constraints Validation
