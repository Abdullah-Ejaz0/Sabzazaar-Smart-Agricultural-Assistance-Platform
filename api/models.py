from django.db import models
from django.utils import timezone
from datetime import timedelta
import uuid


class UserPreference(models.Model):
	phone_number = models.CharField(max_length=25, unique=True)
	preferred_language = models.CharField(max_length=30, blank=True, default='')
	voice_assistant_enabled = models.BooleanField(default=False)
	created_at = models.DateTimeField(auto_now_add=True)
	updated_at = models.DateTimeField(auto_now=True)

	def __str__(self):
		return f"{self.phone_number} ({self.preferred_language or 'not-set'})"


class LanguageOnboardingSession(models.Model):
	token = models.UUIDField(default=uuid.uuid4, unique=True, editable=False)
	preferred_language = models.CharField(max_length=30)
	voice_assistant_enabled = models.BooleanField(default=False)
	created_at = models.DateTimeField(auto_now_add=True)
	expires_at = models.DateTimeField()

	def save(self, *args, **kwargs):
		if not self.expires_at:
			self.expires_at = timezone.now() + timedelta(hours=1)
		super().save(*args, **kwargs)

	def __str__(self):
		return f"{self.token} ({self.preferred_language})"


class ScanImageUpload(models.Model):
	class ImageSource(models.TextChoices):
		CAMERA = 'camera', 'Camera'
		GALLERY = 'gallery', 'Gallery'

	user_preference = models.ForeignKey(
		UserPreference,
		on_delete=models.CASCADE,
		related_name='scan_uploads',
	)
	image = models.ImageField(upload_to='scan_uploads/%Y/%m/%d/')
	source = models.CharField(max_length=20, choices=ImageSource.choices)
	analysis_status = models.CharField(max_length=20, default='pending')
	analysis_result = models.TextField(blank=True, default='')
	created_at = models.DateTimeField(auto_now_add=True)
	updated_at = models.DateTimeField(auto_now=True)

	class Meta:
		ordering = ['-created_at']

	def __str__(self):
		return f"ScanImageUpload #{self.pk} ({self.user_preference.phone_number})"
