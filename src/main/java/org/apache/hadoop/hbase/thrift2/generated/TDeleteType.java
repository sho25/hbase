begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.7.0)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  */
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
comment|/**  * Specify type of delete:  *  - DELETE_COLUMN means exactly one version will be removed,  *  - DELETE_COLUMNS means previous versions will also be removed.  */
end_comment

begin_enum
specifier|public
enum|enum
name|TDeleteType
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
block|{
name|DELETE_COLUMN
argument_list|(
literal|0
argument_list|)
block|,
name|DELETE_COLUMNS
argument_list|(
literal|1
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
specifier|private
name|TDeleteType
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
name|TDeleteType
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
name|DELETE_COLUMN
return|;
case|case
literal|1
case|:
return|return
name|DELETE_COLUMNS
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

