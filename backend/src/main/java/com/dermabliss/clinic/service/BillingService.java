package com.dermabliss.clinic.service;

import com.dermabliss.clinic.dto.CreatePaymentRequest;
import com.dermabliss.clinic.dto.InvoiceDto;
import com.dermabliss.clinic.entity.*;
import com.dermabliss.clinic.exception.InvalidOperationException;
import com.dermabliss.clinic.exception.ResourceNotFoundException;
import com.dermabliss.clinic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public BillingService(InvoiceRepository invoiceRepository,
                          PaymentRepository paymentRepository,
                          PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          UserRepository userRepository,
                          AuditService auditService) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public InvoiceDto createInvoice(Long patientId, Long appointmentId, BigDecimal totalAmount, BigDecimal discountAmount, String notes) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));

        Appointment appointment = null;
        if (appointmentId != null) {
            appointment = appointmentRepository.findById(appointmentId).orElse(null);
        }

        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal net = totalAmount.subtract(discount);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("Discount cannot exceed total invoice amount");
        }

        String invNumber = "INV-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invNumber);
        invoice.setPatient(patient);
        invoice.setAppointment(appointment);
        invoice.setTotalAmount(totalAmount);
        invoice.setDiscountAmount(discount);
        invoice.setNetAmount(net);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setNotes(notes);

        invoice = invoiceRepository.save(invoice);
        auditService.log(null, "INVOICE_CREATED", "invoices", invNumber, "CREATE", null, null, "Invoice created");

        return mapToDto(invoice);
    }

    @Transactional
    public InvoiceDto recordPayment(CreatePaymentRequest req, Long recordedByUserId) {
        Invoice invoice = invoiceRepository.findById(req.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + req.getInvoiceId()));

        User user = userRepository.findById(recordedByUserId).orElse(null);

        String paymentNumber = "PAY-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Payment payment = new Payment();
        payment.setPaymentNumber(paymentNumber);
        payment.setInvoice(invoice);
        payment.setAmount(req.getAmount());
        payment.setPaymentMethod(PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase()));
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setTransactionReference(req.getTransactionReference());
        payment.setNotes(req.getNotes());
        payment.setRecordedByUser(user);

        paymentRepository.save(payment);

        // Update invoice status
        invoice.setStatus(InvoiceStatus.PAID);
        invoice = invoiceRepository.save(invoice);

        auditService.log(user, "PAYMENT_RECORDED", "payments", paymentNumber, "CREATE", null, null, "Payment recorded for " + invoice.getInvoiceNumber());

        return mapToDto(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getPatientInvoices(Long patientId) {
        return invoiceRepository.findByPatientIdOrderByInvoiceDateDesc(patientId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTodayRevenue() {
        return invoiceRepository.sumPaidAmountByDate(LocalDate.now());
    }

    private InvoiceDto mapToDto(Invoice inv) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(inv.getId());
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setPatientId(inv.getPatient().getId());
        dto.setPatientName(inv.getPatient().getFullName());
        dto.setPatientMobile(inv.getPatient().getMobileNumber());
        if (inv.getAppointment() != null) {
            dto.setAppointmentId(inv.getAppointment().getId());
        }
        dto.setTotalAmount(inv.getTotalAmount());
        dto.setDiscountAmount(inv.getDiscountAmount());
        dto.setNetAmount(inv.getNetAmount());
        dto.setStatus(inv.getStatus().name());
        dto.setInvoiceDate(inv.getInvoiceDate());
        dto.setNotes(inv.getNotes());
        dto.setCreatedAt(inv.getCreatedAt());
        return dto;
    }
}
