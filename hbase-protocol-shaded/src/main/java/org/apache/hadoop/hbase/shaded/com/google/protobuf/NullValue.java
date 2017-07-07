begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|// Generated by the protocol buffer compiler.  DO NOT EDIT!
end_comment

begin_comment
comment|// source: google/protobuf/struct.proto
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
package|;
end_package

begin_comment
comment|/**  *<pre>  * `NullValue` is a singleton enumeration to represent the null value for the  * `Value` type union.  *  The JSON representation for `NullValue` is JSON `null`.  *</pre>  *  * Protobuf enum {@code google.protobuf.NullValue}  */
end_comment

begin_enum
specifier|public
enum|enum
name|NullValue
implements|implements
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ProtocolMessageEnum
block|{
comment|/**    *<pre>    * Null value.    *</pre>    *    *<code>NULL_VALUE = 0;</code>    */
name|NULL_VALUE
argument_list|(
literal|0
argument_list|)
block|,
name|UNRECOGNIZED
argument_list|(
operator|-
literal|1
argument_list|)
block|,   ;
comment|/**    *<pre>    * Null value.    *</pre>    *    *<code>NULL_VALUE = 0;</code>    */
specifier|public
specifier|static
specifier|final
name|int
name|NULL_VALUE_VALUE
init|=
literal|0
decl_stmt|;
specifier|public
specifier|final
name|int
name|getNumber
parameter_list|()
block|{
if|if
condition|(
name|this
operator|==
name|UNRECOGNIZED
condition|)
block|{
throw|throw
operator|new
name|java
operator|.
name|lang
operator|.
name|IllegalArgumentException
argument_list|(
literal|"Can't get the number of an unknown enum value."
argument_list|)
throw|;
block|}
return|return
name|value
return|;
block|}
comment|/**    * @deprecated Use {@link #forNumber(int)} instead.    */
annotation|@
name|java
operator|.
name|lang
operator|.
name|Deprecated
specifier|public
specifier|static
name|NullValue
name|valueOf
parameter_list|(
name|int
name|value
parameter_list|)
block|{
return|return
name|forNumber
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|NullValue
name|forNumber
parameter_list|(
name|int
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|value
condition|)
block|{
case|case
literal|0
case|:
return|return
name|NULL_VALUE
return|;
default|default:
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Internal
operator|.
name|EnumLiteMap
argument_list|<
name|NullValue
argument_list|>
name|internalGetValueMap
parameter_list|()
block|{
return|return
name|internalValueMap
return|;
block|}
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Internal
operator|.
name|EnumLiteMap
argument_list|<
name|NullValue
argument_list|>
name|internalValueMap
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Internal
operator|.
name|EnumLiteMap
argument_list|<
name|NullValue
argument_list|>
argument_list|()
block|{
specifier|public
name|NullValue
name|findValueByNumber
parameter_list|(
name|int
name|number
parameter_list|)
block|{
return|return
name|NullValue
operator|.
name|forNumber
argument_list|(
name|number
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|EnumValueDescriptor
name|getValueDescriptor
parameter_list|()
block|{
return|return
name|getDescriptor
argument_list|()
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
name|ordinal
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|EnumDescriptor
name|getDescriptorForType
parameter_list|()
block|{
return|return
name|getDescriptor
argument_list|()
return|;
block|}
specifier|public
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|EnumDescriptor
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|StructProto
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getEnumTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|NullValue
index|[]
name|VALUES
init|=
name|values
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|NullValue
name|valueOf
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Descriptors
operator|.
name|EnumValueDescriptor
name|desc
parameter_list|)
block|{
if|if
condition|(
name|desc
operator|.
name|getType
argument_list|()
operator|!=
name|getDescriptor
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|java
operator|.
name|lang
operator|.
name|IllegalArgumentException
argument_list|(
literal|"EnumValueDescriptor is not for this type."
argument_list|)
throw|;
block|}
if|if
condition|(
name|desc
operator|.
name|getIndex
argument_list|()
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|UNRECOGNIZED
return|;
block|}
return|return
name|VALUES
index|[
name|desc
operator|.
name|getIndex
argument_list|()
index|]
return|;
block|}
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
specifier|private
name|NullValue
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|// @@protoc_insertion_point(enum_scope:google.protobuf.NullValue)
block|}
end_enum

end_unit

