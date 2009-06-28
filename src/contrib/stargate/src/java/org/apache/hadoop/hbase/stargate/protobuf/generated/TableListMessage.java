begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Generated by the protocol buffer compiler.  DO NOT EDIT!
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
name|stargate
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
name|TableListMessage
block|{
specifier|private
name|TableListMessage
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
specifier|static
specifier|final
class|class
name|TableList
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
block|{
comment|// Use TableList.newBuilder() to construct.
specifier|private
name|TableList
parameter_list|()
block|{}
specifier|private
specifier|static
specifier|final
name|TableList
name|defaultInstance
init|=
operator|new
name|TableList
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|TableList
name|getDefaultInstance
parameter_list|()
block|{
return|return
name|defaultInstance
return|;
block|}
specifier|public
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_TableList_descriptor
return|;
block|}
annotation|@
name|Override
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_TableList_fieldAccessorTable
return|;
block|}
comment|// repeated string name = 1;
specifier|public
specifier|static
specifier|final
name|int
name|NAME_FIELD_NUMBER
init|=
literal|1
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|name_
init|=
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|public
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|getNameList
parameter_list|()
block|{
return|return
name|name_
return|;
block|}
specifier|public
name|int
name|getNameCount
parameter_list|()
block|{
return|return
name|name_
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|java
operator|.
name|lang
operator|.
name|String
name|getName
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|name_
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
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
for|for
control|(
name|java
operator|.
name|lang
operator|.
name|String
name|element
range|:
name|getNameList
argument_list|()
control|)
block|{
name|output
operator|.
name|writeString
argument_list|(
literal|1
argument_list|,
name|element
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
annotation|@
name|Override
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
block|{
name|int
name|dataSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|java
operator|.
name|lang
operator|.
name|String
name|element
range|:
name|getNameList
argument_list|()
control|)
block|{
name|dataSize
operator|+=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|CodedOutputStream
operator|.
name|computeStringSizeNoTag
argument_list|(
name|element
argument_list|)
expr_stmt|;
block|}
name|size
operator|+=
name|dataSize
expr_stmt|;
name|size
operator|+=
literal|1
operator|*
name|getNameList
argument_list|()
operator|.
name|size
argument_list|()
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|ExtensionRegistry
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|ExtensionRegistry
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|ExtensionRegistry
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
return|return
name|newBuilder
argument_list|()
operator|.
name|mergeDelimitedFrom
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|ExtensionRegistry
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
name|mergeDelimitedFrom
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|ExtensionRegistry
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
operator|new
name|Builder
argument_list|()
return|;
block|}
specifier|public
name|Builder
name|newBuilderForType
parameter_list|()
block|{
return|return
operator|new
name|Builder
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
name|prototype
parameter_list|)
block|{
return|return
operator|new
name|Builder
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
block|{
comment|// Construct using org.apache.hadoop.hbase.stargate.protobuf.generated.TableListMessage.TableList.newBuilder()
specifier|private
name|Builder
parameter_list|()
block|{}
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
name|internalGetResult
parameter_list|()
block|{
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Builder
name|clear
parameter_list|()
block|{
name|result
operator|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Builder
name|clone
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|result
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
name|build
parameter_list|()
block|{
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
operator|!
name|isInitialized
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UninitializedMessageException
argument_list|(
name|result
argument_list|)
throw|;
block|}
return|return
name|buildPartial
argument_list|()
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
if|if
condition|(
operator|!
name|isInitialized
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UninitializedMessageException
argument_list|(
name|result
argument_list|)
operator|.
name|asInvalidProtocolBufferException
argument_list|()
throw|;
block|}
return|return
name|buildPartial
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
name|buildPartial
parameter_list|()
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"build() has already been called on this Builder."
argument_list|)
throw|;
block|}
if|if
condition|(
name|result
operator|.
name|name_
operator|!=
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|EMPTY_LIST
condition|)
block|{
name|result
operator|.
name|name_
operator|=
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|result
operator|.
name|name_
argument_list|)
expr_stmt|;
block|}
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
name|returnMe
init|=
name|result
decl_stmt|;
name|result
operator|=
literal|null
expr_stmt|;
return|return
name|returnMe
return|;
block|}
annotation|@
name|Override
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
operator|.
name|getDefaultInstance
argument_list|()
condition|)
return|return
name|this
return|;
if|if
condition|(
operator|!
name|other
operator|.
name|name_
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|result
operator|.
name|name_
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|result
operator|.
name|name_
operator|=
operator|new
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|result
operator|.
name|name_
operator|.
name|addAll
argument_list|(
name|other
operator|.
name|name_
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
annotation|@
name|Override
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
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
return|return
name|mergeFrom
argument_list|(
name|input
argument_list|,
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ExtensionRegistry
operator|.
name|getEmptyRegistry
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|ExtensionRegistry
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
return|return
name|this
return|;
block|}
break|break;
block|}
case|case
literal|10
case|:
block|{
name|addName
argument_list|(
name|input
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
comment|// repeated string name = 1;
specifier|public
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|getNameList
parameter_list|()
block|{
return|return
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|result
operator|.
name|name_
argument_list|)
return|;
block|}
specifier|public
name|int
name|getNameCount
parameter_list|()
block|{
return|return
name|result
operator|.
name|getNameCount
argument_list|()
return|;
block|}
specifier|public
name|java
operator|.
name|lang
operator|.
name|String
name|getName
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|result
operator|.
name|getName
argument_list|(
name|index
argument_list|)
return|;
block|}
specifier|public
name|Builder
name|setName
parameter_list|(
name|int
name|index
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
name|result
operator|.
name|name_
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|addName
parameter_list|(
name|java
operator|.
name|lang
operator|.
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
if|if
condition|(
name|result
operator|.
name|name_
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|result
operator|.
name|name_
operator|=
operator|new
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|result
operator|.
name|name_
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|addAllName
parameter_list|(
name|java
operator|.
name|lang
operator|.
name|Iterable
argument_list|<
name|?
extends|extends
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|values
parameter_list|)
block|{
if|if
condition|(
name|result
operator|.
name|name_
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|result
operator|.
name|name_
operator|=
operator|new
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|addAll
argument_list|(
name|values
argument_list|,
name|result
operator|.
name|name_
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clearName
parameter_list|()
block|{
name|result
operator|.
name|name_
operator|=
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
static|static
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|getDescriptor
argument_list|()
expr_stmt|;
block|}
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_TableList_descriptor
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_TableList_fieldAccessorTable
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
name|descriptorData
init|=
literal|"\n\026TableListMessage.proto\0223org.apache.had"
operator|+
literal|"oop.hbase.stargate.protobuf.generated\"\031\n"
operator|+
literal|"\tTableList\022\014\n\004name\030\001 \003(\t"
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_TableList_descriptor
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_TableList_fieldAccessorTable
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_TableList_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Name"
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
name|stargate
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|TableListMessage
operator|.
name|TableList
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
block|}
end_class

end_unit

