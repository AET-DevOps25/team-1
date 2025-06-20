# gRPC Protocol Buffers

This directory contains the Protocol Buffer definitions for gRPC services used in the AIHR system.

## Files

### chat.proto
Defines the ChatService for AI-powered interview conversations.

**Service**: `ChatService`
- **Method**: `GetAIResponse`
- **Purpose**: Send conversation history to GenAI service and receive AI response

**Request**: `ChatRequest`
- `application_id`: UUID of the job application
- `conversation_history`: Array of previous chat messages

**Response**: `ChatResponse`
- `success`: Boolean indicating if the request was successful
- `error_message`: Error description if success is false
- `ai_response`: Generated AI response message

## Usage

### For Java Services (Spring Boot)

1. Add gRPC dependencies to `build.gradle`:
```gradle
implementation 'net.devh:grpc-spring-boot-starter:2.15.0.RELEASE'
implementation 'io.grpc:grpc-netty-shaded:1.58.0'
implementation 'io.grpc:grpc-protobuf:1.58.0'
implementation 'io.grpc:grpc-stub:1.58.0'
```

2. Add protobuf plugin:
```gradle
plugins {
    id 'com.google.protobuf' version '0.9.4'
}
```

3. Configure protobuf compilation:
```gradle
protobuf {
    protoc {
        artifact = 'com.google.protobuf:protoc:3.24.4'
    }
    plugins {
        grpc {
            artifact = 'io.grpc:protoc-gen-grpc-java:1.58.0'
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}
```

### For Python Services (GenAI)

1. Install required packages:
```bash
pip install grpcio grpcio-tools
```

2. Generate Python code:
```bash
python -m grpc_tools.protoc -I./proto --python_out=./service-genai/app --grpc_python_out=./service-genai/app proto/chat.proto
```

## Architecture Flow

```
Frontend → Application Service → gRPC → GenAI Service
                ↓                           ↓
            PostgreSQL                  pgvector + OpenAI
```

1. **Frontend** sends user message to **Application Service**
2. **Application Service** stores user message in database
3. **Application Service** calls **GenAI Service** via gRPC with:
   - `application_id` (GenAI fetches job requirements and resume from DB)
   - `conversation_history` (including the new user message)
4. **GenAI Service** processes the request:
   - Fetches job requirements and candidate resume from database
   - Uses RAG to query vector database (pgvector)
   - Generates AI response using OpenAI API
5. **GenAI Service** returns AI response
6. **Application Service** stores AI response in database
7. **Application Service** returns complete conversation to frontend

## Data Flow

- **Input**: Application ID + Conversation History
- **GenAI Processing**: Job Requirements + Resume + RAG + LLM
- **Output**: AI Response Message 