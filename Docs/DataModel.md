## Metadata

### Billable Items (Facility Service Price)

The first thing you will likely want to set up are the billable items for which you will be charging patients and insurance providers.

At a generic level (not linked to a particular facility or time period), a billable item is first represented by a **Concept**.  This is optional, but recommended in order to be able to link the same billable items across facilities and time periods).

At a particular location, during a particular date range, each billable item is represented by the **FacilityServicePrice** class, and is stored in the **moh_bill_facility_service_price** table.  Entries in this table are used to determine what price to charge for a generic billable item at a given point in time, at a given facility.

    String name -- The name of this item, for this period and location
    String shortName -- A short name for this item.  Unclear how this is used.
    String description -- A description providing more information about this item
    String category -- The category for this item, from a pre-defined supported list
    BigDecimal fullPrice -- The full price of the item to charge
    Concept concept -- The generic billable item that this represents
    Date startDate --  The date at which this price becomes valid
    Date endDate -- The date at which this price is no longer valid
    Location location -- The location at which this price is applicable

**Questions/Issues:**
* What is "shortName" used for?  Is this needed?
* Why is category a String? Shouldn't this be a **Category** enum?
* This needs a uuid, and should extend BaseOpenmrsMetadata
* Are users expected to duplicate data across all locations and periods?

### Insurance plan

An insurance plan is represented by by the **Insurance** class, stored in the **moh_bill_insurance** table.  Many patients will have policies with each insurance plan.

    String name -- The name of the insurer (eg. Mutuelle)
    String address -- The mailing address
    String phone -- The phone number of the insurer
    Concept concept -- Enables linking this insurer to the answer to a coded question
    Set<InsuranceRate> rates -- The rate of coverage, over time
    Set<ServiceCategory> categories -- The categories in which the insurance plan organizes it's billable services
    String category -- High-level type of insurance ("base", "mutuelle", "private", "none")

**Questions/Issues:**
* Is this the correct interpretation of the different categories?
* This needs a uuid, and should extend BaseOpenmrsMetadata
* This should have retireable properties rather than voidable properties
* The category property should be of type InsuranceCategory (enum)

### Insurance Rate

The **InsuranceRate** class, backed by **moh_bill_insurance_rate** table, represents the amount that a given **Insurance** plan covers during a specific period.  This includes both the percent of cost covered, as well as a flat fee, though as of this writing, the flat fee is never used or applied.

    Insurance insurance -- The insurance this is associated with
    Float rate -- The percentage rate that this insurance covers
    BigDecimal flatFee -- A flat fee charge - not used
    Date start_date -- The date at which this rate becomes valid
    Date end_date -- The date at which this rate is no longer valid

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsMetadata
* Flat fee is never used at all.  Can we remove it?

### Service Category

The **ServiceCategory** class, backed by the **moh_bill_service_category** table, represents a categorization of billable services that is supported by a given **Insurance**.  My best current guess is that this exists in order to support the organization of service charges on a claim to an Insurance in the category structure that they require, but this requires confirmation.

    Insurance insurance -- The insurance this is associated with
    String name -- The category name
    String description -- The category description
    BigDecimal price; // Commented as "the capitation price" - I don't think this is used

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsMetadata
* Price does not seem to be used at all.  Can we remove it?
* We need to verify that the code does not make any assumptions that these service categories need to match any of the options listed in the Category enum.  Or if they do need to match in some way, we need to document how.  The different uses of category in this module is the source of some confusion.

### Billable Service

The **BillableService** class, backed by the **moh_bill_billable_service** table, represents the amount that a particular Insurance covers for a specific billable item at a particular facility during a period of time (a FacilityServicePrice).  It also serves as a means to link billable items to a particular ServiceCategory.

    Insurance insurance -- The insurance this is associated with
    FacilityServicePrice facilityServicePrice -- The facility service price this is associated with
    ServiceCategory serviceCategory -- The category of this service with the Insurance
    BigDecimal maximaToPay -- See questions below, not used?
    Date start_date -- The date at which this billable service becomes valid
    Date end_date -- The date at which this billable service is no longer valid

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsMetadata
* From what I can tell, maximaToPay is not actually used anywhere in the system.  There are business logic methods that calculate and set the maximaToPay under certain circumstances based on Insurance category and FacilityServicePrice ServiceCategory (medicaments and consommables = 100%, otherwise base = 100%, mutuelle = 50%, private = 125%, none = 150%), but I don't see this maximaToPay being actually applied to a bill or a report anywhere.

### Third Party

The **ThirdParty** class, backed by the **moh_bill_third_party** table, represents a third party that is associated with a particular Insurance Policy and which is responsible for paying part or all of a bill.

    String name -- The name of the ThirdParty
    Float rate -- The percentage rate that this ThirdParty covers

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsMetadata
* This should have retireable properties rather than voidable properties

## Data

### Insurance Policy

The **InsurancePolicy** class, backed by the **moh_bill_insurance_policy** table, represents a specific policy between a Patient (represented as the "owner" or the policy) and an Insurance plan, valid for a certain period.  An insurance policy has one or more beneficiaries, which include the owner, and typically would also include the owner's spouse and/or children.

    Insurance insurance -- The insurance plan that this policy is associated with
    Patient owner -- The patient who is the owner of this policy
    String insuranceCardNo -- The insurance policy identifier
    Date coverageStartDate -- The date from which the policy is effective
    Date expirationDate -- The date on which this policy ceases to be effective
    ThirdParty thirdParty -- An associated ThirdParty who may be responsible for a portion of coverage
    Set<Beneficiary> beneficiaries - The beneficiaries who can be billed under this policy

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsData
* This should have voidable properties rather than retireable properties

### Beneficiary

The **Beneficiary** class, backed by the **moh_bill_beneficiary** table, represents a Patient covered by a particular Insurance Policy.

    Patient patient -- The Patient who is covered
    InsurancePolicy insurancePolicy -- The Insurance Policy that covers this Patient
    String policyIdNumber -- The identifier of this particular beneficiary under the policy

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsData
* This should have voidable properties rather than retireable properties

### Patient Bill

The **PatientBill** class, backed by the **moh_bill_patient_bill** table, represents a bill for services for a certain amount, linked to a particular Beneficiary.  It contains high-level fields for retrieving the total amount of the bill and the bill payment status, as well as detailed line items for each billable item (see PatientServiceBill below) and payment received (see BillPayment below).

    String description -- This does not appear to be used at all
	  Beneficiary beneficiary -- The Beneficiary who is being billed
	  BigDecimal amount -- This does not appear to be used at all, in favor of on-demand calculation from the billItems
	  boolean printed -- Indicates whether or not the bill was printed
	  boolean isPaid -- Indicates whether or not the bill is paid
	  String status -- The status of the Bill (see BillStatus - FULLY_PAID, PARTLY_PAID, UNPAID)
	  Set<PatientServiceBill> billItems -- The individual line items on the bill
	  Set<BillPayment> payments -- The payments that have been made against this bill

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsData
* The description field does not appear to be in use at all, can remove?
* The amount field does not appear to be in use at all, can remove?
* Status should be of type BillStatus, not a String

### Patient Bill Item

The **PatientServiceBill** class, backed by the **moh_bill_patient_service_bill** table, represents a particular line item on a bill.  This line item should like to a  BillableService that the patient has received, along with the unit price and quantity, which together enables computation of the total cost of the bill item.

    PatientBill patientBill -- The bill which this line item is associated with
    Date serviceDate -- The date on which the service was received
    BillableService service -- The coded service for which to bill
    String serviceOther -- This does not appear to be in use
    String serviceOtherDescription -- This does not appear to be in use
    BigDecimal unitPrice -- The unit cost of the service
    BigDecimal quantity -- The total quantity of the service received

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsData
* The serviceOther and serviceOtherDescription fields do not appear to be in use at all, can remove?

### Bill Payment

The **BillPayment** class, backed by the **moh_bill_payment** table, represents a single payment made against a particular bill.

    PatientBill patientBill -- The bill that this payment is made against
    BigDecimal amountPaid -- The amount that was paid
    Date dateReceived -- The date on which the payment was received
    User collector -- The user who received the payment (typically restricted to users with role of Cashier or Chief Cashier, sometimes Admin)

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsData

### Insurance and Third Party Recovery

The **Recovery** class, backed by the **moh_bill_recovery** table, represents a record of the sum total amount that is due for recovery from an Insurance plan or Third Party payer for all Bills over a particular time period.  This includes the date that the recovery was submitted, the date verified, the current status of the recovery, and the total amounts recovered.

    Insurance insuranceId -- The insurance plan that this recovery is being submitted to
    ThirdParty thirdParty -- The third party payer that this recovery is being submitted to
    Date startPeriod -- The start of the claims period that this recovery represents
    Date endPeriod -- The end of the claims period that this recovery represents
    String status -- Status of the recovery, from RecoveryStatus (SUBMITTED, VERIFIED, FULLYPAID, PARTLYPAID, REFUSED, UNPAID)
    BigDecimal dueAmount -- The total amount due to be recovered
    Date submissionDate -- The date that the recovery was submitted
    Date verificationDate -- The date that the recovery was verified
    BigDecimal paidAmount - The total amount paid by the insurance or third party payer
    Date paymentDate - The date that payment was received
    String partlyPayReason - The reason why payment was only partially made
    String noPaymentReason - The reason why no payment was made
    String observation - Free-text comments or observations to record about the payment status

**Questions/Issues:**
* This needs a uuid, and should extend BaseOpenmrsData
* This should have voidable properties rather than retireable properties

## Print Outs and Reporting

Beyond the core domain and data model above, there are several classes in the data model to assist with generating patient invoices and producing recovery reports:

### Patient Invoice

**PatientInvoice**

    PatientBill patientBill - A reference to a particular Bill
    Map<String, Invoice> invoiceMap - A collection of Invoices, grouped by category
    Double totalAmount - The total amount billed
    Double insuranceCost - The sub-total billed to insurance
    Double patientCost - The sub-total billed to the patient
    Double receivedAmount - The total amount received

**Invoice** - A grouped collection of invoice items that represent a sub-total of a full patient invoice

    Date createdDate - The date the invoice was created
    List<Consommation> consommationList - the list of items on the invoice
    Double subTotal - The total amount of the invoice

**Consommation** - A particular invoice item

    Date recordDate - The date of the invoice item
    String libelle - The name of the Billable Item (FacilityServicePrice name)
    Double unitCost - The unit cost of the item
    BigDecimal quantity - The quantity of items
    Double cost - The total cost (unitCost x quantity)
    Double patientCost - The sub-total cost to the patient
    Double insuranceCost - The sub-total cost to the insurance

### Recovery Reporting

**RecoveryReport** - Represents a line item on the Recovery Report

    String insuranceName - The name of the insurance billed
    Object insuranceDueAmount - The total amount due from the insurance
    float paidAmount - The total amount paid
    float remainingAmount - The total amount remaining
    String startDateStr - The start of the claims period that this recovery represents
    String endDateStr - The end of the claims period that this recovery represents
    String paidDate - The date that the recovery was paid
    int month - The numbered month of the year from the start of the claims period (for the startDateStr)
