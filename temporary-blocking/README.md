
MODULE 1: Request Identification

→ FingerprintFilter

→ FingerprintGenerator

→ RequestContext

MODULE 2: Traffic Control

→ RateLimitFilter

→ RateLimitService

→ Redis

Request flow with this module:

HTTP Request -> FingerprintFilter(IDENTITY) -> RateLimitFilter(CONTROL)->Controller (BUSINESS LOGIC)



