# Test Data & Question Suite
Project: Multi-Tenant AI Knowledge & Decision Assistant

---

## **Organization 1: TechCorp University**
**Associated Knowledge Base PDFs:**
1. `techcorp_policy.pdf` (Student Handbook & Scholarship Rules)
2. `techcorp_admissions.pdf` (Admission Criteria & Fee Structures)

### **Test Questions:**
1. **Direct Info:** "What is the minimum cumulative GPA required for the annual TechCorp Merit Scholarship?"
2. **Direct Info:** "What is the standard late payment fee for tuition?"
3. **Policy Question:** "What are the rules regarding disciplinary probation and scholarship eligibility?"
4. **Procedural Question:** "How far in advance must medical leave be documented and submitted?"
5. **Multi-Condition Question 1:** *"Am I eligible for a merit scholarship if my GPA is 3.5 and my attendance is 90%?"* 
   * *Expected Decision Flow:* Evaluates GPA threshold ($\ge 3.0$) and attendance threshold ($\ge 85\%$) in parallel.
6. **Multiple Structured Decisions / Complex Logic:** *"Can an undergraduate student with a 2.8 GPA and 95% attendance apply for emergency tuition grants, and do they qualify for a merit scholarship?"* 
   * *Expected Decision Flow:* Combines Boolean checks for grant application rights vs. merit scholarship eligibility criteria.
7. **Unanswerable / Out-of-Scope Fallback:** *"Does TechCorp University have an indoor swimming pool or bowling alley?"* 
   * *Expected Result:* Triggers `UNANSWERABLE_OUT_OF_SCOPE` without hallucinating.

---

## **Organization 2: GlobalTech Solutions**
**Associated Knowledge Base PDFs:**
1. `globaltech_hr.pdf` (Corporate HR & Leave Policy Manual)
2. `globaltech_benefits.pdf` (Employee Benefits & Insurance Guide)

### **Test Questions:**
8. **Direct Info:** "How many days of Paid Time Off (PTO) do full-time employees accrue per calendar year?"
9. **Policy Question:** "What insurance coverage is provided to full-time employees on their first day?"
10. **Procedural Question:** "How much is the annual professional development stipend?"
11. **Multi-Condition Question:** *"Can I carry over 7 days of unused PTO into the next calendar year and utilize the professional development stipend for a conference?"* 
   * *Expected Decision Flow:* Evaluates maximum PTO carryover limits against requested days.
12. **Multi-Tenant Isolation Test Question:** *"What is the minimum GPA requirement for the TechCorp merit scholarship?"* 
   * *Expected Result:* Returns out-of-scope/unanswerable because GPA rules belong strictly to Organization 1, proving **strict multi-tenant database isolation**.