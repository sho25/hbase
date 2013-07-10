begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Generated by the protocol buffer compiler.  DO NOT EDIT!
end_comment

begin_comment
comment|// source: Tracing.proto
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
package|;
end_package

begin_class
specifier|public
specifier|final
class|class
name|Tracing
block|{
specifier|private
name|Tracing
parameter_list|()
block|{}
specifier|public
specifier|static
name|void
name|registerAllExtensions
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistry
name|registry
parameter_list|)
block|{   }
specifier|public
interface|interface
name|RPCTInfoOrBuilder
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|MessageOrBuilder
block|{
comment|// optional int64 trace_id = 1;
name|boolean
name|hasTraceId
parameter_list|()
function_decl|;
name|long
name|getTraceId
parameter_list|()
function_decl|;
comment|// optional int64 parent_id = 2;
name|boolean
name|hasParentId
parameter_list|()
function_decl|;
name|long
name|getParentId
parameter_list|()
function_decl|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|RPCTInfo
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
implements|implements
name|RPCTInfoOrBuilder
block|{
comment|// Use RPCTInfo.newBuilder() to construct.
specifier|private
name|RPCTInfo
parameter_list|(
name|Builder
name|builder
parameter_list|)
block|{
name|super
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RPCTInfo
parameter_list|(
name|boolean
name|noInit
parameter_list|)
block|{}
specifier|private
specifier|static
specifier|final
name|RPCTInfo
name|defaultInstance
decl_stmt|;
specifier|public
specifier|static
name|RPCTInfo
name|getDefaultInstance
parameter_list|()
block|{
return|return
name|defaultInstance
return|;
block|}
specifier|public
name|RPCTInfo
name|getDefaultInstanceForType
parameter_list|()
block|{
return|return
name|defaultInstance
return|;
block|}
specifier|public
specifier|static
specifier|final
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|internal_static_RPCTInfo_descriptor
return|;
block|}
specifier|protected
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
operator|.
name|FieldAccessorTable
name|internalGetFieldAccessorTable
parameter_list|()
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|internal_static_RPCTInfo_fieldAccessorTable
return|;
block|}
specifier|private
name|int
name|bitField0_
decl_stmt|;
comment|// optional int64 trace_id = 1;
specifier|public
specifier|static
specifier|final
name|int
name|TRACE_ID_FIELD_NUMBER
init|=
literal|1
decl_stmt|;
specifier|private
name|long
name|traceId_
decl_stmt|;
specifier|public
name|boolean
name|hasTraceId
parameter_list|()
block|{
return|return
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000001
operator|)
operator|==
literal|0x00000001
operator|)
return|;
block|}
specifier|public
name|long
name|getTraceId
parameter_list|()
block|{
return|return
name|traceId_
return|;
block|}
comment|// optional int64 parent_id = 2;
specifier|public
specifier|static
specifier|final
name|int
name|PARENT_ID_FIELD_NUMBER
init|=
literal|2
decl_stmt|;
specifier|private
name|long
name|parentId_
decl_stmt|;
specifier|public
name|boolean
name|hasParentId
parameter_list|()
block|{
return|return
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000002
operator|)
operator|==
literal|0x00000002
operator|)
return|;
block|}
specifier|public
name|long
name|getParentId
parameter_list|()
block|{
return|return
name|parentId_
return|;
block|}
specifier|private
name|void
name|initFields
parameter_list|()
block|{
name|traceId_
operator|=
literal|0L
expr_stmt|;
name|parentId_
operator|=
literal|0L
expr_stmt|;
block|}
specifier|private
name|byte
name|memoizedIsInitialized
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
specifier|final
name|boolean
name|isInitialized
parameter_list|()
block|{
name|byte
name|isInitialized
init|=
name|memoizedIsInitialized
decl_stmt|;
if|if
condition|(
name|isInitialized
operator|!=
operator|-
literal|1
condition|)
return|return
name|isInitialized
operator|==
literal|1
return|;
name|memoizedIsInitialized
operator|=
literal|1
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|writeTo
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
name|output
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
name|getSerializedSize
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000001
operator|)
operator|==
literal|0x00000001
operator|)
condition|)
block|{
name|output
operator|.
name|writeInt64
argument_list|(
literal|1
argument_list|,
name|traceId_
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000002
operator|)
operator|==
literal|0x00000002
operator|)
condition|)
block|{
name|output
operator|.
name|writeInt64
argument_list|(
literal|2
argument_list|,
name|parentId_
argument_list|)
expr_stmt|;
block|}
name|getUnknownFields
argument_list|()
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|memoizedSerializedSize
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|int
name|getSerializedSize
parameter_list|()
block|{
name|int
name|size
init|=
name|memoizedSerializedSize
decl_stmt|;
if|if
condition|(
name|size
operator|!=
operator|-
literal|1
condition|)
return|return
name|size
return|;
name|size
operator|=
literal|0
expr_stmt|;
if|if
condition|(
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000001
operator|)
operator|==
literal|0x00000001
operator|)
condition|)
block|{
name|size
operator|+=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
operator|.
name|computeInt64Size
argument_list|(
literal|1
argument_list|,
name|traceId_
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000002
operator|)
operator|==
literal|0x00000002
operator|)
condition|)
block|{
name|size
operator|+=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
operator|.
name|computeInt64Size
argument_list|(
literal|2
argument_list|,
name|parentId_
argument_list|)
expr_stmt|;
block|}
name|size
operator|+=
name|getUnknownFields
argument_list|()
operator|.
name|getSerializedSize
argument_list|()
expr_stmt|;
name|memoizedSerializedSize
operator|=
name|size
expr_stmt|;
return|return
name|size
return|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|0L
decl_stmt|;
annotation|@
name|java
operator|.
name|lang
operator|.
name|Override
specifier|protected
name|java
operator|.
name|lang
operator|.
name|Object
name|writeReplace
parameter_list|()
throws|throws
name|java
operator|.
name|io
operator|.
name|ObjectStreamException
block|{
return|return
name|super
operator|.
name|writeReplace
argument_list|()
return|;
block|}
annotation|@
name|java
operator|.
name|lang
operator|.
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|java
operator|.
name|lang
operator|.
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|)
condition|)
block|{
return|return
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
return|;
block|}
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|other
init|=
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|)
name|obj
decl_stmt|;
name|boolean
name|result
init|=
literal|true
decl_stmt|;
name|result
operator|=
name|result
operator|&&
operator|(
name|hasTraceId
argument_list|()
operator|==
name|other
operator|.
name|hasTraceId
argument_list|()
operator|)
expr_stmt|;
if|if
condition|(
name|hasTraceId
argument_list|()
condition|)
block|{
name|result
operator|=
name|result
operator|&&
operator|(
name|getTraceId
argument_list|()
operator|==
name|other
operator|.
name|getTraceId
argument_list|()
operator|)
expr_stmt|;
block|}
name|result
operator|=
name|result
operator|&&
operator|(
name|hasParentId
argument_list|()
operator|==
name|other
operator|.
name|hasParentId
argument_list|()
operator|)
expr_stmt|;
if|if
condition|(
name|hasParentId
argument_list|()
condition|)
block|{
name|result
operator|=
name|result
operator|&&
operator|(
name|getParentId
argument_list|()
operator|==
name|other
operator|.
name|getParentId
argument_list|()
operator|)
expr_stmt|;
block|}
name|result
operator|=
name|result
operator|&&
name|getUnknownFields
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getUnknownFields
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|java
operator|.
name|lang
operator|.
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hash
init|=
literal|41
decl_stmt|;
name|hash
operator|=
operator|(
literal|19
operator|*
name|hash
operator|)
operator|+
name|getDescriptorForType
argument_list|()
operator|.
name|hashCode
argument_list|()
expr_stmt|;
if|if
condition|(
name|hasTraceId
argument_list|()
condition|)
block|{
name|hash
operator|=
operator|(
literal|37
operator|*
name|hash
operator|)
operator|+
name|TRACE_ID_FIELD_NUMBER
expr_stmt|;
name|hash
operator|=
operator|(
literal|53
operator|*
name|hash
operator|)
operator|+
name|hashLong
argument_list|(
name|getTraceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasParentId
argument_list|()
condition|)
block|{
name|hash
operator|=
operator|(
literal|37
operator|*
name|hash
operator|)
operator|+
name|PARENT_ID_FIELD_NUMBER
expr_stmt|;
name|hash
operator|=
operator|(
literal|53
operator|*
name|hash
operator|)
operator|+
name|hashLong
argument_list|(
name|getParentId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|hash
operator|=
operator|(
literal|29
operator|*
name|hash
operator|)
operator|+
name|getUnknownFields
argument_list|()
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|hash
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|data
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|data
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|data
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|data
argument_list|,
name|extensionRegistry
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|data
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|data
argument_list|,
name|extensionRegistry
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|java
operator|.
name|io
operator|.
name|InputStream
name|input
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|input
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|java
operator|.
name|io
operator|.
name|InputStream
name|input
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|input
argument_list|,
name|extensionRegistry
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseDelimitedFrom
parameter_list|(
name|java
operator|.
name|io
operator|.
name|InputStream
name|input
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
name|Builder
name|builder
init|=
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|builder
operator|.
name|mergeDelimitedFrom
argument_list|(
name|input
argument_list|)
condition|)
block|{
return|return
name|builder
operator|.
name|buildParsed
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseDelimitedFrom
parameter_list|(
name|java
operator|.
name|io
operator|.
name|InputStream
name|input
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
name|Builder
name|builder
init|=
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|builder
operator|.
name|mergeDelimitedFrom
argument_list|(
name|input
argument_list|,
name|extensionRegistry
argument_list|)
condition|)
block|{
return|return
name|builder
operator|.
name|buildParsed
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedInputStream
name|input
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|input
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|parseFrom
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedInputStream
name|input
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|input
argument_list|,
name|extensionRegistry
argument_list|)
operator|.
name|buildParsed
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Builder
name|newBuilder
parameter_list|()
block|{
return|return
name|Builder
operator|.
name|create
argument_list|()
return|;
block|}
specifier|public
name|Builder
name|newBuilderForType
parameter_list|()
block|{
return|return
name|newBuilder
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Builder
name|newBuilder
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|prototype
parameter_list|)
block|{
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|prototype
argument_list|)
return|;
block|}
specifier|public
name|Builder
name|toBuilder
parameter_list|()
block|{
return|return
name|newBuilder
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|java
operator|.
name|lang
operator|.
name|Override
specifier|protected
name|Builder
name|newBuilderForType
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
operator|.
name|BuilderParent
name|parent
parameter_list|)
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|parent
argument_list|)
decl_stmt|;
return|return
name|builder
return|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|Builder
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|>
implements|implements
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfoOrBuilder
block|{
specifier|public
specifier|static
specifier|final
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|internal_static_RPCTInfo_descriptor
return|;
block|}
specifier|protected
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
operator|.
name|FieldAccessorTable
name|internalGetFieldAccessorTable
parameter_list|()
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|internal_static_RPCTInfo_fieldAccessorTable
return|;
block|}
comment|// Construct using org.apache.hadoop.hbase.protobuf.generated.Tracing.RPCTInfo.newBuilder()
specifier|private
name|Builder
parameter_list|()
block|{
name|maybeForceBuilderInitialization
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Builder
parameter_list|(
name|BuilderParent
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|maybeForceBuilderInitialization
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|maybeForceBuilderInitialization
parameter_list|()
block|{
if|if
condition|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
operator|.
name|alwaysUseFieldBuilders
condition|)
block|{         }
block|}
specifier|private
specifier|static
name|Builder
name|create
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
return|;
block|}
specifier|public
name|Builder
name|clear
parameter_list|()
block|{
name|super
operator|.
name|clear
argument_list|()
expr_stmt|;
name|traceId_
operator|=
literal|0L
expr_stmt|;
name|bitField0_
operator|=
operator|(
name|bitField0_
operator|&
operator|~
literal|0x00000001
operator|)
expr_stmt|;
name|parentId_
operator|=
literal|0L
expr_stmt|;
name|bitField0_
operator|=
operator|(
name|bitField0_
operator|&
operator|~
literal|0x00000002
operator|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clone
parameter_list|()
block|{
return|return
name|create
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|buildPartial
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|getDescriptorForType
parameter_list|()
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|.
name|getDescriptor
argument_list|()
return|;
block|}
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|getDefaultInstanceForType
parameter_list|()
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|.
name|getDefaultInstance
argument_list|()
return|;
block|}
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|build
parameter_list|()
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|result
init|=
name|buildPartial
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|result
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
throw|throw
name|newUninitializedMessageException
argument_list|(
name|result
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|buildParsed
parameter_list|()
throws|throws
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|result
init|=
name|buildPartial
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|result
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
throw|throw
name|newUninitializedMessageException
argument_list|(
name|result
argument_list|)
operator|.
name|asInvalidProtocolBufferException
argument_list|()
throw|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|buildPartial
parameter_list|()
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|result
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|int
name|from_bitField0_
init|=
name|bitField0_
decl_stmt|;
name|int
name|to_bitField0_
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|(
operator|(
name|from_bitField0_
operator|&
literal|0x00000001
operator|)
operator|==
literal|0x00000001
operator|)
condition|)
block|{
name|to_bitField0_
operator||=
literal|0x00000001
expr_stmt|;
block|}
name|result
operator|.
name|traceId_
operator|=
name|traceId_
expr_stmt|;
if|if
condition|(
operator|(
operator|(
name|from_bitField0_
operator|&
literal|0x00000002
operator|)
operator|==
literal|0x00000002
operator|)
condition|)
block|{
name|to_bitField0_
operator||=
literal|0x00000002
expr_stmt|;
block|}
name|result
operator|.
name|parentId_
operator|=
name|parentId_
expr_stmt|;
name|result
operator|.
name|bitField0_
operator|=
name|to_bitField0_
expr_stmt|;
name|onBuilt
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|Builder
name|mergeFrom
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|instanceof
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
condition|)
block|{
return|return
name|mergeFrom
argument_list|(
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|)
name|other
argument_list|)
return|;
block|}
else|else
block|{
name|super
operator|.
name|mergeFrom
argument_list|(
name|other
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
specifier|public
name|Builder
name|mergeFrom
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|.
name|getDefaultInstance
argument_list|()
condition|)
return|return
name|this
return|;
if|if
condition|(
name|other
operator|.
name|hasTraceId
argument_list|()
condition|)
block|{
name|setTraceId
argument_list|(
name|other
operator|.
name|getTraceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|hasParentId
argument_list|()
condition|)
block|{
name|setParentId
argument_list|(
name|other
operator|.
name|getParentId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|mergeUnknownFields
argument_list|(
name|other
operator|.
name|getUnknownFields
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
specifier|final
name|boolean
name|isInitialized
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|Builder
name|mergeFrom
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedInputStream
name|input
parameter_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistryLite
name|extensionRegistry
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnknownFieldSet
operator|.
name|Builder
name|unknownFields
init|=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnknownFieldSet
operator|.
name|newBuilder
argument_list|(
name|this
operator|.
name|getUnknownFields
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|tag
init|=
name|input
operator|.
name|readTag
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|tag
condition|)
block|{
case|case
literal|0
case|:
name|this
operator|.
name|setUnknownFields
argument_list|(
name|unknownFields
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
default|default:
block|{
if|if
condition|(
operator|!
name|parseUnknownField
argument_list|(
name|input
argument_list|,
name|unknownFields
argument_list|,
name|extensionRegistry
argument_list|,
name|tag
argument_list|)
condition|)
block|{
name|this
operator|.
name|setUnknownFields
argument_list|(
name|unknownFields
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
break|break;
block|}
case|case
literal|8
case|:
block|{
name|bitField0_
operator||=
literal|0x00000001
expr_stmt|;
name|traceId_
operator|=
name|input
operator|.
name|readInt64
argument_list|()
expr_stmt|;
break|break;
block|}
case|case
literal|16
case|:
block|{
name|bitField0_
operator||=
literal|0x00000002
expr_stmt|;
name|parentId_
operator|=
name|input
operator|.
name|readInt64
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
specifier|private
name|int
name|bitField0_
decl_stmt|;
comment|// optional int64 trace_id = 1;
specifier|private
name|long
name|traceId_
decl_stmt|;
specifier|public
name|boolean
name|hasTraceId
parameter_list|()
block|{
return|return
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000001
operator|)
operator|==
literal|0x00000001
operator|)
return|;
block|}
specifier|public
name|long
name|getTraceId
parameter_list|()
block|{
return|return
name|traceId_
return|;
block|}
specifier|public
name|Builder
name|setTraceId
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|bitField0_
operator||=
literal|0x00000001
expr_stmt|;
name|traceId_
operator|=
name|value
expr_stmt|;
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clearTraceId
parameter_list|()
block|{
name|bitField0_
operator|=
operator|(
name|bitField0_
operator|&
operator|~
literal|0x00000001
operator|)
expr_stmt|;
name|traceId_
operator|=
literal|0L
expr_stmt|;
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// optional int64 parent_id = 2;
specifier|private
name|long
name|parentId_
decl_stmt|;
specifier|public
name|boolean
name|hasParentId
parameter_list|()
block|{
return|return
operator|(
operator|(
name|bitField0_
operator|&
literal|0x00000002
operator|)
operator|==
literal|0x00000002
operator|)
return|;
block|}
specifier|public
name|long
name|getParentId
parameter_list|()
block|{
return|return
name|parentId_
return|;
block|}
specifier|public
name|Builder
name|setParentId
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|bitField0_
operator||=
literal|0x00000002
expr_stmt|;
name|parentId_
operator|=
name|value
expr_stmt|;
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clearParentId
parameter_list|()
block|{
name|bitField0_
operator|=
operator|(
name|bitField0_
operator|&
operator|~
literal|0x00000002
operator|)
expr_stmt|;
name|parentId_
operator|=
literal|0L
expr_stmt|;
name|onChanged
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// @@protoc_insertion_point(builder_scope:RPCTInfo)
block|}
static|static
block|{
name|defaultInstance
operator|=
operator|new
name|RPCTInfo
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|defaultInstance
operator|.
name|initFields
argument_list|()
expr_stmt|;
block|}
comment|// @@protoc_insertion_point(class_scope:RPCTInfo)
block|}
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|Descriptor
name|internal_static_RPCTInfo_descriptor
decl_stmt|;
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
operator|.
name|FieldAccessorTable
name|internal_static_RPCTInfo_fieldAccessorTable
decl_stmt|;
specifier|public
specifier|static
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|getDescriptor
parameter_list|()
block|{
return|return
name|descriptor
return|;
block|}
specifier|private
specifier|static
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|descriptor
decl_stmt|;
static|static
block|{
name|java
operator|.
name|lang
operator|.
name|String
index|[]
name|descriptorData
init|=
block|{
literal|"\n\rTracing.proto\"/\n\010RPCTInfo\022\020\n\010trace_id\030"
operator|+
literal|"\001 \001(\003\022\021\n\tparent_id\030\002 \001(\003B:\n*org.apache.h"
operator|+
literal|"adoop.hbase.protobuf.generatedB\007TracingH"
operator|+
literal|"\001\240\001\001"
block|}
decl_stmt|;
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|InternalDescriptorAssigner
name|assigner
init|=
operator|new
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|InternalDescriptorAssigner
argument_list|()
block|{
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistry
name|assignDescriptors
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
name|root
parameter_list|)
block|{
name|descriptor
operator|=
name|root
expr_stmt|;
name|internal_static_RPCTInfo_descriptor
operator|=
name|getDescriptor
argument_list|()
operator|.
name|getMessageTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|internal_static_RPCTInfo_fieldAccessorTable
operator|=
operator|new
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
operator|.
name|FieldAccessorTable
argument_list|(
name|internal_static_RPCTInfo_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"TraceId"
block|,
literal|"ParentId"
block|, }
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|.
name|class
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|Tracing
operator|.
name|RPCTInfo
operator|.
name|Builder
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
operator|.
name|internalBuildGeneratedFileFrom
argument_list|(
name|descriptorData
argument_list|,
operator|new
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|FileDescriptor
index|[]
block|{         }
argument_list|,
name|assigner
argument_list|)
expr_stmt|;
block|}
comment|// @@protoc_insertion_point(outer_class_scope)
block|}
end_class

end_unit

