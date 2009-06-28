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
name|CellMessage
block|{
specifier|private
name|CellMessage
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
name|Cell
extends|extends
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|GeneratedMessage
block|{
comment|// Use Cell.newBuilder() to construct.
specifier|private
name|Cell
parameter_list|()
block|{}
specifier|private
specifier|static
specifier|final
name|Cell
name|defaultInstance
init|=
operator|new
name|Cell
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|Cell
name|getDefaultInstance
parameter_list|()
block|{
return|return
name|defaultInstance
return|;
block|}
specifier|public
name|Cell
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
name|CellMessage
operator|.
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_Cell_descriptor
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
name|CellMessage
operator|.
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_Cell_fieldAccessorTable
return|;
block|}
comment|// optional bytes row = 1;
specifier|public
specifier|static
specifier|final
name|int
name|ROW_FIELD_NUMBER
init|=
literal|1
decl_stmt|;
specifier|private
name|boolean
name|hasRow
decl_stmt|;
specifier|private
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|row_
init|=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
operator|.
name|EMPTY
decl_stmt|;
specifier|public
name|boolean
name|hasRow
parameter_list|()
block|{
return|return
name|hasRow
return|;
block|}
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|getRow
parameter_list|()
block|{
return|return
name|row_
return|;
block|}
comment|// optional bytes column = 2;
specifier|public
specifier|static
specifier|final
name|int
name|COLUMN_FIELD_NUMBER
init|=
literal|2
decl_stmt|;
specifier|private
name|boolean
name|hasColumn
decl_stmt|;
specifier|private
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|column_
init|=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
operator|.
name|EMPTY
decl_stmt|;
specifier|public
name|boolean
name|hasColumn
parameter_list|()
block|{
return|return
name|hasColumn
return|;
block|}
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|getColumn
parameter_list|()
block|{
return|return
name|column_
return|;
block|}
comment|// optional int64 timestamp = 3;
specifier|public
specifier|static
specifier|final
name|int
name|TIMESTAMP_FIELD_NUMBER
init|=
literal|3
decl_stmt|;
specifier|private
name|boolean
name|hasTimestamp
decl_stmt|;
specifier|private
name|long
name|timestamp_
init|=
literal|0L
decl_stmt|;
specifier|public
name|boolean
name|hasTimestamp
parameter_list|()
block|{
return|return
name|hasTimestamp
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp_
return|;
block|}
comment|// optional bytes data = 4;
specifier|public
specifier|static
specifier|final
name|int
name|DATA_FIELD_NUMBER
init|=
literal|4
decl_stmt|;
specifier|private
name|boolean
name|hasData
decl_stmt|;
specifier|private
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|data_
init|=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
operator|.
name|EMPTY
decl_stmt|;
specifier|public
name|boolean
name|hasData
parameter_list|()
block|{
return|return
name|hasData
return|;
block|}
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|getData
parameter_list|()
block|{
return|return
name|data_
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
if|if
condition|(
name|hasRow
argument_list|()
condition|)
block|{
name|output
operator|.
name|writeBytes
argument_list|(
literal|1
argument_list|,
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasColumn
argument_list|()
condition|)
block|{
name|output
operator|.
name|writeBytes
argument_list|(
literal|2
argument_list|,
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasTimestamp
argument_list|()
condition|)
block|{
name|output
operator|.
name|writeInt64
argument_list|(
literal|3
argument_list|,
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasData
argument_list|()
condition|)
block|{
name|output
operator|.
name|writeBytes
argument_list|(
literal|4
argument_list|,
name|getData
argument_list|()
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
if|if
condition|(
name|hasRow
argument_list|()
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
name|computeBytesSize
argument_list|(
literal|1
argument_list|,
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasColumn
argument_list|()
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
name|computeBytesSize
argument_list|(
literal|2
argument_list|,
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasTimestamp
argument_list|()
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
literal|3
argument_list|,
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasData
argument_list|()
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
name|computeBytesSize
argument_list|(
literal|4
argument_list|,
name|getData
argument_list|()
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
comment|// Construct using org.apache.hadoop.hbase.stargate.protobuf.generated.CellMessage.Cell.newBuilder()
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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
name|hasRow
argument_list|()
condition|)
block|{
name|setRow
argument_list|(
name|other
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|hasColumn
argument_list|()
condition|)
block|{
name|setColumn
argument_list|(
name|other
operator|.
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|hasTimestamp
argument_list|()
condition|)
block|{
name|setTimestamp
argument_list|(
name|other
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|hasData
argument_list|()
condition|)
block|{
name|setData
argument_list|(
name|other
operator|.
name|getData
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
name|setRow
argument_list|(
name|input
operator|.
name|readBytes
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
literal|18
case|:
block|{
name|setColumn
argument_list|(
name|input
operator|.
name|readBytes
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
literal|24
case|:
block|{
name|setTimestamp
argument_list|(
name|input
operator|.
name|readInt64
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
literal|34
case|:
block|{
name|setData
argument_list|(
name|input
operator|.
name|readBytes
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
comment|// optional bytes row = 1;
specifier|public
name|boolean
name|hasRow
parameter_list|()
block|{
return|return
name|result
operator|.
name|hasRow
argument_list|()
return|;
block|}
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|getRow
parameter_list|()
block|{
return|return
name|result
operator|.
name|getRow
argument_list|()
return|;
block|}
specifier|public
name|Builder
name|setRow
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|hasRow
operator|=
literal|true
expr_stmt|;
name|result
operator|.
name|row_
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clearRow
parameter_list|()
block|{
name|result
operator|.
name|hasRow
operator|=
literal|false
expr_stmt|;
name|result
operator|.
name|row_
operator|=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
operator|.
name|EMPTY
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// optional bytes column = 2;
specifier|public
name|boolean
name|hasColumn
parameter_list|()
block|{
return|return
name|result
operator|.
name|hasColumn
argument_list|()
return|;
block|}
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|getColumn
parameter_list|()
block|{
return|return
name|result
operator|.
name|getColumn
argument_list|()
return|;
block|}
specifier|public
name|Builder
name|setColumn
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|hasColumn
operator|=
literal|true
expr_stmt|;
name|result
operator|.
name|column_
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clearColumn
parameter_list|()
block|{
name|result
operator|.
name|hasColumn
operator|=
literal|false
expr_stmt|;
name|result
operator|.
name|column_
operator|=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
operator|.
name|EMPTY
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// optional int64 timestamp = 3;
specifier|public
name|boolean
name|hasTimestamp
parameter_list|()
block|{
return|return
name|result
operator|.
name|hasTimestamp
argument_list|()
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|result
operator|.
name|getTimestamp
argument_list|()
return|;
block|}
specifier|public
name|Builder
name|setTimestamp
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|result
operator|.
name|hasTimestamp
operator|=
literal|true
expr_stmt|;
name|result
operator|.
name|timestamp_
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clearTimestamp
parameter_list|()
block|{
name|result
operator|.
name|hasTimestamp
operator|=
literal|false
expr_stmt|;
name|result
operator|.
name|timestamp_
operator|=
literal|0L
expr_stmt|;
return|return
name|this
return|;
block|}
comment|// optional bytes data = 4;
specifier|public
name|boolean
name|hasData
parameter_list|()
block|{
return|return
name|result
operator|.
name|hasData
argument_list|()
return|;
block|}
specifier|public
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
name|getData
parameter_list|()
block|{
return|return
name|result
operator|.
name|getData
argument_list|()
return|;
block|}
specifier|public
name|Builder
name|setData
parameter_list|(
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|hasData
operator|=
literal|true
expr_stmt|;
name|result
operator|.
name|data_
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|clearData
parameter_list|()
block|{
name|result
operator|.
name|hasData
operator|=
literal|false
expr_stmt|;
name|result
operator|.
name|data_
operator|=
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
operator|.
name|EMPTY
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
name|CellMessage
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_Cell_descriptor
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_Cell_fieldAccessorTable
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
literal|"\n\021CellMessage.proto\0223org.apache.hadoop.h"
operator|+
literal|"base.stargate.protobuf.generated\"D\n\004Cell"
operator|+
literal|"\022\013\n\003row\030\001 \001(\014\022\016\n\006column\030\002 \001(\014\022\021\n\ttimesta"
operator|+
literal|"mp\030\003 \001(\003\022\014\n\004data\030\004 \001(\014"
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_Cell_descriptor
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_Cell_fieldAccessorTable
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
name|internal_static_org_apache_hadoop_hbase_stargate_protobuf_generated_Cell_descriptor
argument_list|,
operator|new
name|java
operator|.
name|lang
operator|.
name|String
index|[]
block|{
literal|"Row"
block|,
literal|"Column"
block|,
literal|"Timestamp"
block|,
literal|"Data"
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
name|CellMessage
operator|.
name|Cell
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
name|CellMessage
operator|.
name|Cell
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

