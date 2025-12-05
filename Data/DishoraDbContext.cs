using System;
using System.Collections.Generic;
using Dishora.Data;
using Dishora.Models;
using Microsoft.EntityFrameworkCore;

namespace Dishora.Data;

public partial class DishoraDbContext : DbContext
{
    public DishoraDbContext(DbContextOptions<DishoraDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<business_details> business_details { get; set; }

    public virtual DbSet<business_opening_hours> business_opening_hours { get; set; }

    public virtual DbSet<business_payment_methods> business_payment_methods { get; set; }

    public virtual DbSet<business_pm_details> business_pm_details { get; set; }

    public virtual DbSet<cache> caches { get; set; }

    public virtual DbSet<cache_locks> cache_locks { get; set; }

    public virtual DbSet<checkout_drafts> checkout_drafts { get; set; }

    public virtual DbSet<customers> customers { get; set; }

    public virtual DbSet<delivery_addresses> delivery_addresses { get; set; }

    public virtual DbSet<device_tokens> device_tokens { get; set; }

    public virtual DbSet<dietary_specifications> dietary_specifications { get; set; }

    public virtual DbSet<failed_jobs> failed_jobs { get; set; }

    public virtual DbSet<jobs> jobs { get; set; }

    public virtual DbSet<job_batches> job_batches { get; set; }

    public virtual DbSet<messages> messages { get; set; }

    public virtual DbSet<migration> migrations { get; set; }

    public virtual DbSet<notifications> notifications { get; set; }

    public virtual DbSet<notification_deliveries> notification_deliveries { get; set; }

    public virtual DbSet<notification_preferences> notification_preferences { get; set; }


    public virtual DbSet<orders> orders { get; set; }

    public virtual DbSet<order_items> order_items { get; set; }

    public virtual DbSet<order_sessions> order_sessions { get; set; }

    public virtual DbSet<password_reset_tokens> password_reset_tokens { get; set; }

    public virtual DbSet<payment_details> payment_details { get; set; }

    public virtual DbSet<payment_methods> payment_methods { get; set; }

    public virtual DbSet<pre_orders> pre_orders { get; set; }

    public virtual DbSet<preorder_schedule> preorder_schedules { get; set; }

    public virtual DbSet<products> products { get; set; }

    public virtual DbSet<product_categories> product_categories { get; set; }

    public virtual DbSet<product_dietary_specifications> product_dietary_specifications { get; set; }

    public virtual DbSet<reviews> reviews { get; set; }

    public virtual DbSet<sessions> sessions { get; set; }

    public virtual DbSet<users> users { get; set; }

    public virtual DbSet<valid_ids> valid_ids { get; set; }

    public virtual DbSet<vendors> vendors { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<business_details>(entity =>
        {
            entity.HasKey(e => e.business_id).HasName("PK__business__DC0DC16ED09F57DA");

            entity.ToTable("business_details");

            entity.HasIndex(e => e.business_name, "business_details_business_name_unique").IsUnique();

            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.bir_reg_file)
                .HasMaxLength(255)
                .HasColumnName("bir_reg_file");
            entity.Property(e => e.bir_reg_no)
                .HasMaxLength(50)
                .HasColumnName("bir_reg_no");
            entity.Property(e => e.business_description).HasColumnName("business_description");
            entity.Property(e => e.business_duration)
                .HasMaxLength(255)
                .HasColumnName("business_duration");
            entity.Property(e => e.business_image)
                .HasMaxLength(255)
                .HasColumnName("business_image");
            entity.Property(e => e.business_location).HasColumnName("business_location");
            entity.Property(e => e.business_name)
                .HasMaxLength(150)
                .HasColumnName("business_name");
            entity.Property(e => e.business_permit_file)
                .HasMaxLength(255)
                .HasColumnName("business_permit_file");
            entity.Property(e => e.business_permit_no)
                .HasMaxLength(50)
                .HasColumnName("business_permit_no");
            entity.Property(e => e.business_type)
                .HasMaxLength(150)
                .HasColumnName("business_type");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.latitude).HasColumnName("latitude");
            entity.Property(e => e.longitude).HasColumnName("longitude");
            entity.Property(e => e.mayor_permit_file)
                .HasMaxLength(255)
                .HasColumnName("mayor_permit_file");
            entity.Property(e => e.preorder_lead_time_hours)
                .HasDefaultValueSql("('48')")
                .HasColumnName("preorder_lead_time_hours");
            entity.Property(e => e.remarks)
                .HasMaxLength(255)
                .HasColumnName("remarks");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.valid_id_file)
                .HasMaxLength(255)
                .HasColumnName("valid_id_file");
            entity.Property(e => e.valid_id_no)
                .HasMaxLength(50)
                .HasColumnName("valid_id_no");
            entity.Property(e => e.valid_id_type)
                .HasMaxLength(50)
                .HasColumnName("valid_id_type");
            entity.Property(e => e.vendor_id).HasColumnName("vendor_id");
            entity.Property(e => e.verification_status)
                .HasMaxLength(255)
                .HasDefaultValue("Pending")
                .HasColumnName("verification_status");

            entity.HasOne(d => d.vendor).WithMany(p => p.BusinessDetails)
                .HasForeignKey(d => d.vendor_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("business_details_vendor_id_foreign");
        });

        modelBuilder.Entity<business_opening_hours>(entity =>
        {
            entity.HasKey(e => e.business_opening_hours_id).HasName("PK__business__07C3205740894924");

            entity.ToTable("business_opening_hours");

            entity.Property(e => e.business_opening_hours_id).HasColumnName("business_opening_hours_id");
            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.closes_at).HasColumnName("closes_at");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.day_of_week)
                .HasMaxLength(255)
                .HasColumnName("day_of_week");
            entity.Property(e => e.is_closed)
                .IsRequired()
                .HasDefaultValueSql("('0')")
                .HasColumnName("is_closed");
            entity.Property(e => e.opens_at).HasColumnName("opens_at");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.business).WithMany(p => p.opening_hours)
                .HasForeignKey(d => d.business_id)
                .HasConstraintName("business_opening_hours_business_id_foreign");
        });

        modelBuilder.Entity<business_payment_methods>(entity =>
        {
            entity.HasKey(e => e.business_payment_method_id).HasName("PK__business__609F6EF9D0092684");

            entity.ToTable("business_payment_methods");

            entity.HasIndex(e => new { e.business_id, e.payment_method_id }, "business_payment_methods_business_id_payment_method_id_unique").IsUnique();

            entity.Property(e => e.business_payment_method_id).HasColumnName("business_payment_method_id");
            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.payment_method_id).HasColumnName("payment_method_id");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.business).WithMany(p => p.payment_methods)
                .HasForeignKey(d => d.business_id)
                .HasConstraintName("business_payment_methods_business_id_foreign");

            entity.HasOne(d => d.payment_method).WithMany(p => p.BusinessPaymentMethods)
                .HasForeignKey(d => d.payment_method_id)
                .HasConstraintName("business_payment_methods_payment_method_id_foreign");
        });

        modelBuilder.Entity<business_pm_details>(entity =>
        {
            entity.HasKey(e => e.business_pm_details_id).HasName("PK__business__83D9FB0C2FB4D7A1");

            entity.ToTable("business_pm_details");

            entity.HasIndex(e => e.business_payment_method_id, "UQ_business_payment_method_id").IsUnique();

            entity.Property(e => e.business_pm_details_id).HasColumnName("business_pm_details_id");
            entity.Property(e => e.account_name)
                .HasMaxLength(255)
                .HasColumnName("account_name");
            entity.Property(e => e.account_number)
                .HasMaxLength(255)
                .HasColumnName("account_number");
            entity.Property(e => e.business_payment_method_id).HasColumnName("business_payment_method_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.is_active)
                .IsRequired()
                .HasDefaultValueSql("('1')")
                .HasColumnName("is_active");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.business_payment_method)
                .WithOne(p => p.business_pm_detail) // Use the new singular property
                .HasForeignKey<business_pm_details>(d => d.business_payment_method_id)
                .OnDelete(DeleteBehavior.Cascade) // Optional: Deletes details if parent is deleted
                .HasConstraintName("business_pm_details_business_payment_method_id_foreign");
        });

        modelBuilder.Entity<cache>(entity =>
        {
            entity.HasKey(e => e.key).HasName("cache_key_primary");

            entity.ToTable("cache");

            entity.Property(e => e.key)
                .HasMaxLength(255)
                .HasColumnName("key");
            entity.Property(e => e.expiration).HasColumnName("expiration");
            entity.Property(e => e.value).HasColumnName("value");
        });

        modelBuilder.Entity<cache_locks>(entity =>
        {
            entity.HasKey(e => e.key).HasName("cache_locks_key_primary");

            entity.ToTable("cache_locks");

            entity.Property(e => e.key)
                .HasMaxLength(255)
                .HasColumnName("key");
            entity.Property(e => e.expiration).HasColumnName("expiration");
            entity.Property(e => e.owner)
                .HasMaxLength(255)
                .HasColumnName("owner");
        });

        modelBuilder.Entity<checkout_drafts>(entity =>
        {
            entity.HasKey(e => e.checkout_draft_id).HasName("PK__checkout__BEC51E282769ED44");

            entity.ToTable("checkout_drafts");

            entity.Property(e => e.checkout_draft_id).HasColumnName("checkout_draft_id");
            entity.Property(e => e.cart).HasColumnName("cart");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.delivery).HasColumnName("delivery");
            entity.Property(e => e.is_cod)
                .IsRequired()
                .HasDefaultValueSql("('0')")
                .HasColumnName("is_cod");
            entity.Property(e => e.item_notes).HasColumnName("item_notes");
            entity.Property(e => e.payment_method_id).HasColumnName("payment_method_id");
            entity.Property(e => e.processed_at)
                .HasColumnType("datetime")
                .HasColumnName("processed_at");
            entity.Property(e => e.total)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("total");
            entity.Property(e => e.transaction_id)
                .HasMaxLength(255)
                .HasColumnName("transaction_id");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.user_id).HasColumnName("user_id");
        });

        modelBuilder.Entity<customers>(entity =>
        {
            entity.HasKey(e => e.customer_id).HasName("PK__customer__CD65CB85F8307575");

            entity.ToTable("customers");

            entity.Property(e => e.customer_id).HasColumnName("customer_id");
            entity.Property(e => e.contact_number)
                .HasMaxLength(20)
                .HasColumnName("contact_number");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.latitude).HasColumnName("latitude");
            entity.Property(e => e.longitude).HasColumnName("longitude");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.user_address).HasColumnName("user_address");
            entity.Property(e => e.user_id).HasColumnName("user_id");
            entity.Property(e => e.user_image)
                .HasMaxLength(255)
                .HasColumnName("user_image");

            entity.HasOne(d => d.user).WithMany(p => p.customers)
                .HasForeignKey(d => d.user_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("customers_user_id_foreign");
        });

        modelBuilder.Entity<delivery_addresses>(entity =>
        {
            entity.HasKey(e => e.delivery_address_id).HasName("PK__delivery__B55B0D223CDBEAE2");

            entity.ToTable("delivery_addresses");

            entity.HasIndex(e => e.city, "delivery_addresses_city_index");

            entity.HasIndex(e => e.postal_code, "delivery_addresses_postal_code_index");

            entity.HasIndex(e => e.province, "delivery_addresses_province_index");

            entity.Property(e => e.delivery_address_id).HasColumnName("delivery_address_id");
            entity.Property(e => e.barangay)
                .HasMaxLength(255)
                .HasColumnName("barangay");
            entity.Property(e => e.city)
                .HasMaxLength(255)
                .HasColumnName("city");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.full_address).HasColumnName("full_address");
            entity.Property(e => e.order_id).HasColumnName("order_id");
            entity.Property(e => e.phone_number)
                .HasMaxLength(20)
                .HasColumnName("phone_number");
            entity.Property(e => e.postal_code)
                .HasMaxLength(20)
                .HasColumnName("postal_code");
            entity.Property(e => e.province)
                .HasMaxLength(255)
                .HasColumnName("province");
            entity.Property(e => e.region)
                .HasMaxLength(255)
                .HasColumnName("region");
            entity.Property(e => e.street_name).HasColumnName("street_name");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.user_id).HasColumnName("user_id");

            entity.HasOne(d => d.Order).WithMany(p => p.delivery_address)
                .HasForeignKey(d => d.order_id)
                .HasConstraintName("delivery_addresses_order_id_foreign");

            entity.HasOne(d => d.User).WithMany(p => p.deliveryaddresses)
                .HasForeignKey(d => d.user_id)
                .HasConstraintName("delivery_addresses_user_id_foreign");
        });

        modelBuilder.Entity<device_tokens>(entity =>
        {
            entity.HasKey(e => e.device_token_id).HasName("PK__device_t__3ADABB7DD781558C");

            entity.ToTable("device_tokens");

            entity.HasIndex(e => new { e.user_id, e.is_active }, "IX_device_tokens_user_active");

            entity.Property(e => e.device_token_id).HasColumnName("device_token_id");
            entity.Property(e => e.created_at)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.is_active)
                .HasDefaultValue(true)
                .HasColumnName("is_active");
            entity.Property(e => e.last_seen)
                .HasColumnType("datetime")
                .HasColumnName("last_seen");
            entity.Property(e => e.platform)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("platform");
            entity.Property(e => e.provider)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("provider");
            entity.Property(e => e.sns_endpoint_arn).HasColumnName("sns_endpoint_arn");
            entity.Property(e => e.token)
                .HasMaxLength(500)
                .IsUnicode(false)
                .HasColumnName("token");
            entity.Property(e => e.user_id).HasColumnName("user_id");

            entity.HasOne(d => d.User).WithMany(p => p.devicetokens)
                .HasForeignKey(d => d.user_id)
                .HasConstraintName("FK_device_tokens_user");
        });

        modelBuilder.Entity<dietary_specifications>(entity =>
        {
            entity.HasKey(e => e.dietary_specification_id).HasName("PK__dietary___7B2CC1C5F5F03468");

            entity.ToTable("dietary_specifications");

            entity.Property(e => e.dietary_specification_id).HasColumnName("dietary_specification_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.dietary_spec_name)
                .HasMaxLength(255)
                .HasColumnName("dietary_spec_name");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
        });

        modelBuilder.Entity<failed_jobs>(entity =>
        {
            entity.HasKey(e => e.id).HasName("PK__failed_j__3213E83F8AAC85BF");

            entity.ToTable("failed_jobs");

            entity.HasIndex(e => e.uu_id, "failed_jobs_uuid_unique").IsUnique();

            entity.Property(e => e.id).HasColumnName("id");
            entity.Property(e => e.connection).HasColumnName("connection");
            entity.Property(e => e.exception).HasColumnName("exception");
            entity.Property(e => e.failed_at)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("failed_at");
            entity.Property(e => e.payload).HasColumnName("payload");
            entity.Property(e => e.queue).HasColumnName("queue");
            entity.Property(e => e.uu_id)
                .HasMaxLength(255)
                .HasColumnName("uuid");
        });

        modelBuilder.Entity<jobs>(entity =>
        {
            entity.HasKey(e => e.id).HasName("PK__jobs__3213E83FDC0633ED");

            entity.ToTable("jobs");

            entity.HasIndex(e => e.queue, "jobs_queue_index");

            entity.Property(e => e.id).HasColumnName("id");
            entity.Property(e => e.attempts).HasColumnName("attempts");
            entity.Property(e => e.available_at).HasColumnName("available_at");
            entity.Property(e => e.created_at).HasColumnName("created_at");
            entity.Property(e => e.payload).HasColumnName("payload");
            entity.Property(e => e.queue)
                .HasMaxLength(255)
                .HasColumnName("queue");
            entity.Property(e => e.reserved_at).HasColumnName("reserved_at");
        });

        modelBuilder.Entity<job_batches>(entity =>
        {
            entity.HasKey(e => e.id).HasName("job_batches_id_primary");

            entity.ToTable("job_batches");

            entity.Property(e => e.id)
                .HasMaxLength(255)
                .HasColumnName("id");
            entity.Property(e => e.cancelled_at).HasColumnName("cancelled_at");
            entity.Property(e => e.created_at).HasColumnName("created_at");
            entity.Property(e => e.failed_job_ids).HasColumnName("failed_job_ids");
            entity.Property(e => e.failed_jobs).HasColumnName("failed_jobs");
            entity.Property(e => e.finished_at).HasColumnName("finished_at");
            entity.Property(e => e.name)
                .HasMaxLength(255)
                .HasColumnName("name");
            entity.Property(e => e.options).HasColumnName("options");
            entity.Property(e => e.pending_jobs).HasColumnName("pending_jobs");
            entity.Property(e => e.total_jobs).HasColumnName("total_jobs");
        });

        modelBuilder.Entity<messages>(entity =>
        {
            entity.HasKey(e => e.message_id).HasName("PK__messages__0BBF6EE6A246B939");

            entity.ToTable("messages");

            entity.HasIndex(e => new { e.receiver_id, e.receiver_role }, "messages_receiver_id_receiver_role_index");

            entity.HasIndex(e => new { e.sender_id, e.sender_role }, "messages_sender_id_sender_role_index");

            entity.Property(e => e.message_id).HasColumnName("message_id");
            entity.Property(e => e.image_url)
                .HasMaxLength(255)
                .HasColumnName("image_url");
            entity.Property(e => e.is_read)
                .IsRequired()
                .HasDefaultValueSql("('0')")
                .HasColumnName("is_read");
            entity.Property(e => e.message_text).HasColumnName("message_text");
            entity.Property(e => e.receiver_id).HasColumnName("receiver_id");
            entity.Property(e => e.receiver_role)
                .HasMaxLength(255)
                .HasColumnName("receiver_role");
            entity.Property(e => e.sender_id).HasColumnName("sender_id");
            entity.Property(e => e.sender_role)
                .HasMaxLength(255)
                .HasColumnName("sender_role");
            entity.Property(e => e.sent_at)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("sent_at");
        });

        modelBuilder.Entity<migration>(entity =>
        {
            entity.HasKey(e => e.id).HasName("PK__migratio__3213E83F940046B0");

            entity.ToTable("migrations");

            entity.Property(e => e.id).HasColumnName("id");
            entity.Property(e => e.batch).HasColumnName("batch");
            entity.Property(e => e.migration1)
                .HasMaxLength(255)
                .HasColumnName("migration");
        });

        modelBuilder.Entity<notifications>(entity =>
        {
            entity.HasKey(e => e.notification_id).HasName("PK__notifica__E059842F31B9C5FC");

            entity.ToTable("notifications");

            entity.HasIndex(e => new { e.user_id, e.is_read, e.created_at }, "IX_notifications_user_read_created");

            entity.Property(e => e.notification_id).HasColumnName("notification_id");
            entity.Property(e => e.actor_user_id).HasColumnName("actor_user_id");
            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.channel)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasDefaultValue("in_app")
                .HasColumnName("channel");
            entity.Property(e => e.created_at)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.event_type)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("event_type");
            entity.Property(e => e.expires_at)
                .HasColumnType("datetime")
                .HasColumnName("expires_at");
            entity.Property(e => e.is_global).HasColumnName("is_global");
            entity.Property(e => e.is_read).HasColumnName("is_read");
            entity.Property(e => e.payload).HasColumnName("payload");
            entity.Property(e => e.recipient_role)
                .HasMaxLength(32)
                .IsUnicode(false)
                .HasColumnName("recipient_role");
            entity.Property(e => e.reference_id).HasColumnName("reference_id");
            entity.Property(e => e.reference_table)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("reference_table");
            entity.Property(e => e.user_id).HasColumnName("user_id");

            entity.HasOne(d => d.User).WithMany(p => p.notifications)
                .HasForeignKey(d => d.user_id)
                .HasConstraintName("FK_notifications_user");
        });

        modelBuilder.Entity<notification_deliveries>(entity =>
        {
            entity.HasKey(e => e.delivery_id).HasName("PK__notifica__1C5CF4F525371EB5");

            entity.ToTable("notification_deliveries");

            entity.Property(e => e.delivery_id).HasColumnName("delivery_id");
            entity.Property(e => e.attempted_at)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("attempted_at");
            entity.Property(e => e.notification_id).HasColumnName("notification_id");
            entity.Property(e => e.provider)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("provider");
            entity.Property(e => e.provider_response).HasColumnName("provider_response");
            entity.Property(e => e.success).HasColumnName("success");

            entity.HasOne(d => d.notification).WithMany(p => p.notificationdeliveries)
                .HasForeignKey(d => d.notification_id)
                .HasConstraintName("FK_notification_deliveries_notification");
        });

        modelBuilder.Entity<notification_preferences>(entity =>
        {
            entity.HasKey(e => e.preference_id).HasName("PK__notifica__FB41DBCF922A6C67");

            entity.ToTable("notification_preferences");

            entity.HasIndex(e => new { e.user_id, e.event_type, e.channel }, "UC_notification_pref").IsUnique();

            entity.Property(e => e.preference_id).HasColumnName("preference_id");
            entity.Property(e => e.channel)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("channel");
            entity.Property(e => e.enabled)
                .HasDefaultValue(true)
                .HasColumnName("enabled");
            entity.Property(e => e.event_type)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("event_type");
            entity.Property(e => e.updated_at)
                .HasDefaultValueSql("(getdate())")
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.user_id).HasColumnName("user_id");

            entity.HasOne(d => d.User).WithMany(p => p.notificationpreferences)
                .HasForeignKey(d => d.user_id)
                .HasConstraintName("FK_notification_preferences_user");
        });

        modelBuilder.Entity<orders>(entity =>
        {
            entity.HasKey(e => e.order_id).HasName("PK__orders__46596229259F2E55");

            entity.ToTable("orders");

            entity.Property(e => e.order_id).HasColumnName("order_id");
            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.cancellation_reason)
                .HasMaxLength(255)
                .IsUnicode(false)
                .HasColumnName("cancellation_reason");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.delivery_date).HasColumnName("delivery_date");
            entity.Property(e => e.delivery_time)
                .HasMaxLength(255)
                .HasColumnName("delivery_time");
            entity.Property(e => e.payment_method_id).HasColumnName("payment_method_id");
            entity.Property(e => e.proof_of_delivery)
                .HasMaxLength(255)
                .HasColumnName("proof_of_delivery");
            entity.Property(e => e.total)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("total");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.user_id).HasColumnName("user_id");

            entity.HasOne(d => d.business).WithMany(p => p.orders)
                .HasForeignKey(d => d.business_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("orders_business_id_foreign");

            entity.HasOne(d => d.payment_method).WithMany(p => p.orders)
                .HasForeignKey(d => d.payment_method_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("orders_payment_method_id_foreign");

            entity.HasOne(d => d.User).WithMany(p => p.orders)
                .HasForeignKey(d => d.user_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("orders_user_id_foreign");
        });

        modelBuilder.Entity<order_items>(entity =>
        {
            entity.HasKey(e => e.order_item_id).HasName("PK__order_it__3764B6BCCCB802C6");

            entity.ToTable("order_items");

            entity.Property(e => e.order_item_id).HasColumnName("order_item_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.is_pre_order)
                .IsRequired()
                .HasDefaultValueSql("('0')")
                .HasColumnName("is_pre_order");
            entity.Property(e => e.order_id).HasColumnName("order_id");
            entity.Property(e => e.order_item_note).HasColumnName("order_item_note");
            entity.Property(e => e.order_item_status)
                .HasMaxLength(255)
                .HasDefaultValue("Pending")
                .HasColumnName("order_item_status");
            entity.Property(e => e.price_at_order_time)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("price_at_order_time");
            entity.Property(e => e.product_description).HasColumnName("product_description");
            entity.Property(e => e.product_id).HasColumnName("product_id");
            entity.Property(e => e.product_name)
                .HasMaxLength(255)
                .HasColumnName("product_name");
            entity.Property(e => e.quantity).HasColumnName("quantity");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.order).WithMany(p => p.order_item)
                .HasForeignKey(d => d.order_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("order_items_order_id_foreign");

            entity.HasOne(d => d.product).WithMany(p => p.order_items)
                .HasForeignKey(d => d.product_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("order_items_product_id_foreign");
        });

        modelBuilder.Entity<order_sessions>(entity =>
        {
            entity.HasKey(e => e.order_session_id).HasName("PK__order_se__48134B5EC839221C");

            entity.ToTable("order_sessions");

            entity.Property(e => e.order_session_id).HasColumnName("order_session_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.orders).HasColumnName("orders");
            entity.Property(e => e.session_id)
                .HasMaxLength(255)
                .HasColumnName("session_id");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.user_id).HasColumnName("user_id");
        });

        modelBuilder.Entity<password_reset_tokens>(entity =>
        {
            entity.HasKey(e => e.email).HasName("password_reset_tokens_email_primary");

            entity.ToTable("password_reset_tokens");

            entity.Property(e => e.email)
                .HasMaxLength(255)
                .HasColumnName("email");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.token)
                .HasMaxLength(255)
                .HasColumnName("token");
        });

        modelBuilder.Entity<payment_details>(entity =>
        {
            entity.HasKey(e => e.payment_detail_id).HasName("PK__payment___C66E6E368AD9887A");

            entity.ToTable("payment_details");

            entity.Property(e => e.payment_detail_id).HasColumnName("payment_detail_id");
            entity.Property(e => e.amount_paid)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("amount_paid");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.order_id).HasColumnName("order_id");
            entity.Property(e => e.paid_at)
                .HasColumnType("datetime")
                .HasColumnName("paid_at");
            entity.Property(e => e.payment_method_id).HasColumnName("payment_method_id");
            entity.Property(e => e.payment_reference)
                .HasMaxLength(100)
                .HasColumnName("payment_reference");
            entity.Property(e => e.payment_status)
                .HasMaxLength(255)
                .HasDefaultValue("Pending")
                .HasColumnName("payment_status");
            entity.Property(e => e.transaction_id)
                .HasMaxLength(200)
                .HasColumnName("transaction_id");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.order).WithMany(p => p.payment_detail)
                .HasForeignKey(d => d.order_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("payment_details_order_id_foreign");

            entity.HasOne(d => d.payment_method).WithMany(p => p.payment_details)
                .HasForeignKey(d => d.payment_method_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("payment_details_payment_method_id_foreign");
        });

        modelBuilder.Entity<payment_methods>(entity =>
        {
            entity.HasKey(e => e.payment_method_id).HasName("PK__payment___8A3EA9EB4A7D74F4");

            entity.ToTable("payment_methods");

            entity.Property(e => e.payment_method_id).HasColumnName("payment_method_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.description)
                .HasMaxLength(255)
                .HasColumnName("description");
            entity.Property(e => e.method_name)
                .HasMaxLength(200)
                .HasColumnName("method_name");
            entity.Property(e => e.status)
                .HasMaxLength(255)
                .HasDefaultValue("active")
                .HasColumnName("status");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
        });

        modelBuilder.Entity<pre_orders>(entity =>
        {
            entity.HasKey(e => e.pre_order_id).HasName("PK__pre_orde__FC60B68D38DC36D5");

            entity.ToTable("pre_orders");

            entity.HasIndex(e => e.order_id, "UQ_pre_orders_order_id").IsUnique();

            entity.Property(e => e.pre_order_id).HasColumnName("pre_order_id");
            entity.Property(e => e.advance_paid_amount)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("advance_paid_amount");
            entity.Property(e => e.amount_due)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("amount_due");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.order_id).HasColumnName("order_id");
            entity.Property(e => e.payment_option)
                .HasMaxLength(255)
                .HasColumnName("payment_option");
            entity.Property(e => e.payment_transaction_id)
                .HasMaxLength(255)
                .HasColumnName("payment_transaction_id");
            entity.Property(e => e.preorder_status)
                .HasMaxLength(255)
                .HasDefaultValue("pending_payment")
                .HasColumnName("preorder_status");
            entity.Property(e => e.receipt_url)
                .HasMaxLength(255)
                .HasColumnName("receipt_url");
            entity.Property(e => e.total_advance_required)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("total_advance_required");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.Order).WithOne(p => p.preorder)
                .HasForeignKey<pre_orders>(d => d.order_id)
                .HasConstraintName("pre_orders_order_id_foreign");
        });

        modelBuilder.Entity<preorder_schedule>(entity =>
        {
            entity.HasKey(e => e.schedule_id).HasName("PK__preorder__C46A8A6FD8342062");

            entity.ToTable("preorder_schedule");

            entity.HasIndex(e => new { e.business_id, e.available_date }, "preorder_schedule_business_id_available_date_unique").IsUnique();

            entity.Property(e => e.schedule_id).HasColumnName("schedule_id");
            entity.Property(e => e.available_date).HasColumnName("available_date");
            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.current_order_count)
                .HasDefaultValueSql("('0')")
                .HasColumnName("current_order_count");
            entity.Property(e => e.is_active)
                .IsRequired()
                .HasDefaultValueSql("('1')")
                .HasColumnName("is_active");
            entity.Property(e => e.max_orders).HasColumnName("max_orders");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.business).WithMany(p => p.preorder_schedule)
                .HasForeignKey(d => d.business_id)
                .HasConstraintName("preorder_schedule_business_id_foreign");
        });

        modelBuilder.Entity<products>(entity =>
        {
            entity.HasKey(e => e.product_id).HasName("PK__products__47027DF5730B421E");

            entity.ToTable("products");

            entity.Property(e => e.product_id).HasColumnName("product_id");
            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.advance_amount)
                .HasDefaultValueSql("('0')")
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("advance_amount");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.cutoff_minutes).HasColumnName("cutoff_minutes");
            entity.Property(e => e.description).HasColumnName("description");
            entity.Property(e => e.image_url)
                .HasMaxLength(255)
                .HasColumnName("image_url");
            entity.Property(e => e.is_available)
                .IsRequired()
                .HasDefaultValueSql("('1')")
                .HasColumnName("is_available");
            entity.Property(e => e.is_pre_order)
                .IsRequired()
                .HasDefaultValueSql("('0')")
                .HasColumnName("is_pre_order");
            entity.Property(e => e.item_name)
                .HasMaxLength(100)
                .HasColumnName("item_name");
            entity.Property(e => e.price)
                .HasColumnType("decimal(10, 2)")
                .HasColumnName("price");
            entity.Property(e => e.product_category_id).HasColumnName("product_category_id");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.business).WithMany(p => p.products)
                .HasForeignKey(d => d.business_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("products_business_id_foreign");

            entity.HasOne(d => d.product_category).WithMany(p => p.products)
                .HasForeignKey(d => d.product_category_id)
                .HasConstraintName("products_product_category_id_foreign");
        });

        modelBuilder.Entity<product_categories>(entity =>
        {
            entity.HasKey(e => e.product_category_id).HasName("PK__product___1F8847F92E946C37");

            entity.ToTable("product_categories");

            entity.Property(e => e.product_category_id).HasColumnName("product_category_id");
            entity.Property(e => e.category_name)
                .HasMaxLength(100)
                .HasColumnName("category_name");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
        });

        modelBuilder.Entity<product_dietary_specifications>(entity =>
        {
            entity.HasKey(e => new { e.product_id, e.dietary_specification_id }).HasName("product_dietary_specifications_product_id_dietary_specification_id_primary");

            entity.ToTable("product_dietary_specifications");

            entity.Property(e => e.product_id).HasColumnName("product_id");
            entity.Property(e => e.dietary_specification_id).HasColumnName("dietary_specification_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.dietary_specification).WithMany(p => p.product_dietary_specifications)
                .HasForeignKey(d => d.dietary_specification_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("product_dietary_specifications_dietary_specification_id_foreign");

            entity.HasOne(d => d.product).WithMany(p => p.product_dietary_specifications)
                .HasForeignKey(d => d.product_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("product_dietary_specifications_product_id_foreign");
        });

        modelBuilder.Entity<reviews>(entity =>
        {
            entity.HasKey(e => e.review_id).HasName("PK__reviews__60883D90D2154919");

            entity.ToTable("reviews");

            entity.Property(e => e.review_id).HasColumnName("review_id");
            entity.Property(e => e.business_id).HasColumnName("business_id");
            entity.Property(e => e.comment)
                .HasMaxLength(255)
                .HasColumnName("comment");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.customer_id).HasColumnName("customer_id");
            entity.Property(e => e.rating).HasColumnName("rating");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.business).WithMany(p => p.reviews)
                .HasForeignKey(d => d.business_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("reviews_business_id_foreign");

            entity.HasOne(d => d.customer).WithMany(p => p.reviews)
                .HasForeignKey(d => d.customer_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("reviews_customer_id_foreign");
        });

        modelBuilder.Entity<sessions>(entity =>
        {
            entity.HasKey(e => e.id).HasName("sessions_id_primary");

            entity.ToTable("sessions");

            entity.HasIndex(e => e.last_activity, "sessions_last_activity_index");

            entity.HasIndex(e => e.user_id, "sessions_user_id_index");

            entity.Property(e => e.id)
                .HasMaxLength(255)
                .HasColumnName("id");
            entity.Property(e => e.ip_address)
                .HasMaxLength(45)
                .HasColumnName("ip_address");
            entity.Property(e => e.last_activity).HasColumnName("last_activity");
            entity.Property(e => e.payload).HasColumnName("payload");
            entity.Property(e => e.user_agent).HasColumnName("user_agent");
            entity.Property(e => e.user_id).HasColumnName("user_id");
        });

        modelBuilder.Entity<users>(entity =>
        {
            entity.HasKey(e => e.user_id).HasName("PK__users__B9BE370F29D3F97B");

            entity.ToTable("users");

            entity.HasIndex(e => e.email, "users_email_unique").IsUnique();

            entity.HasIndex(e => e.username, "users_username_unique").IsUnique();

            entity.Property(e => e.user_id).HasColumnName("user_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.email)
                .HasMaxLength(255)
                .HasColumnName("email");
            entity.Property(e => e.email_verified_at)
                .HasColumnType("datetime")
                .HasColumnName("email_verified_at");
            entity.Property(e => e.fullname)
                .HasMaxLength(255)
                .HasColumnName("fullname");
            entity.Property(e => e.is_verified)
                .IsRequired()
                .HasDefaultValueSql("('0')")
                .HasColumnName("is_verified");
            entity.Property(e => e.password)
                .HasMaxLength(255)
                .HasColumnName("password");
            entity.Property(e => e.remember_token)
                .HasMaxLength(100)
                .HasColumnName("remember_token");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.username)
                .HasMaxLength(255)
                .HasColumnName("username");
            entity.Property(e => e.verification_token)
                .HasMaxLength(255)
                .HasColumnName("verification_token");

            //entity.HasMany(d => d.roles).WithMany(p => p.UserRole)
            //    .UsingEntity<Dictionary<string, object>>(
            //        "UserRole",
            //        r => r.HasOne<Role>().WithMany()
            //            .HasForeignKey("RoleId")
            //            .HasConstraintName("user_roles_role_id_foreign"),
            //        l => l.HasOne<User>().WithMany()
            //            .HasForeignKey("UserId")
            //            .HasConstraintName("user_roles_user_id_foreign"),
            //        j =>
            //        {
            //            j.HasKey("UserId", "RoleId").HasName("user_roles_user_id_role_id_primary");
            //            j.ToTable("user_roles");
            //            j.IndexerProperty<long>("UserId").HasColumnName("user_id");
            //            j.IndexerProperty<long>("RoleId").HasColumnName("role_id");
            //        });
        });

        modelBuilder.Entity<valid_ids>(entity =>
        {
            entity.HasKey(e => e.valid_id_id).HasName("PK__valid_id__FCBC6B5B1980E305");

            entity.ToTable("valid_ids");

            entity.Property(e => e.valid_id_id).HasColumnName("valid_id_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.valid_id_name)
                .HasMaxLength(100)
                .HasColumnName("valid_id_name");
        });

        modelBuilder.Entity<vendors>(entity =>
        {
            entity.HasKey(e => e.vendor_id).HasName("PK__vendors__0F7D2B789F3A74BA");

            entity.ToTable("vendors");

            entity.HasIndex(e => e.user_id, "vendors_user_id_unique").IsUnique();

            entity.Property(e => e.vendor_id).HasColumnName("vendor_id");
            entity.Property(e => e.created_at)
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.fullname)
                .HasMaxLength(150)
                .HasColumnName("fullname");
            entity.Property(e => e.phone_number)
                .HasMaxLength(20)
                .HasColumnName("phone_number");
            entity.Property(e => e.registration_status)
                .HasMaxLength(255)
                .HasDefaultValue("Pending")
                .HasColumnName("registration_status");
            entity.Property(e => e.updated_at)
                .HasColumnType("datetime")
                .HasColumnName("updated_at");
            entity.Property(e => e.user_id).HasColumnName("user_id");

            entity.HasOne(d => d.User).WithOne(p => p.vendor)
                .HasForeignKey<vendors>(d => d.user_id)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("vendors_user_id_foreign");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
