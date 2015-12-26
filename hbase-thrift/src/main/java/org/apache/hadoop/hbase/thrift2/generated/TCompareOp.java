begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.9.3)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  *  @generated  */
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
name|thrift2
operator|.
name|generated
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
import|;
end_import

begin_comment
comment|/**  * Thrift wrapper around  * org.apache.hadoop.hbase.filter.CompareFilter$CompareOp.  */
end_comment

begin_enum
specifier|public
enum|enum
name|TCompareOp
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
block|{
name|LESS
argument_list|(
literal|0
argument_list|)
block|,
name|LESS_OR_EQUAL
argument_list|(
literal|1
argument_list|)
block|,
name|EQUAL
argument_list|(
literal|2
argument_list|)
block|,
name|NOT_EQUAL
argument_list|(
literal|3
argument_list|)
block|,
name|GREATER_OR_EQUAL
argument_list|(
literal|4
argument_list|)
block|,
name|GREATER
argument_list|(
literal|5
argument_list|)
block|,
name|NO_OP
argument_list|(
literal|6
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
specifier|private
name|TCompareOp
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
comment|/**    * Get the integer value of this enum value, as defined in the Thrift IDL.    */
specifier|public
name|int
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**    * Find a the enum type by its integer value, as defined in the Thrift IDL.    * @return null if the value is not found.    */
specifier|public
specifier|static
name|TCompareOp
name|findByValue
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
name|LESS
return|;
case|case
literal|1
case|:
return|return
name|LESS_OR_EQUAL
return|;
case|case
literal|2
case|:
return|return
name|EQUAL
return|;
case|case
literal|3
case|:
return|return
name|NOT_EQUAL
return|;
case|case
literal|4
case|:
return|return
name|GREATER_OR_EQUAL
return|;
case|case
literal|5
case|:
return|return
name|GREATER
return|;
case|case
literal|6
case|:
return|return
name|NO_OP
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
end_enum

end_unit

