local tokenKey = KEYS[1]
local timeKey = KEYS[2]

local maxTokens = tonumber(ARGV[1])
local refillInterval = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local tokens = tonumber(redis.call("GET", tokenKey))
local lastRefill = tonumber(redis.call("GET", timeKey))

-- First request → initialize bucket
if tokens == nil then
    tokens = maxTokens
    lastRefill = now
end

-- Refill logic
local elapsed = now - lastRefill
local tokensToAdd = math.floor(elapsed / refillInterval)

if tokensToAdd > 0 then
    tokens = math.min(maxTokens, tokens + tokensToAdd)
    lastRefill = lastRefill + (tokensToAdd * refillInterval)
end

-- If no tokens → reject
if tokens <= 0 then
    redis.call("SET", tokenKey, tokens)
    redis.call("SET", timeKey, lastRefill)
    return 0
end

-- Consume token
tokens = tokens - 1

-- Persist state
redis.call("SET", tokenKey, tokens)
redis.call("SET", timeKey, lastRefill)

return 1
