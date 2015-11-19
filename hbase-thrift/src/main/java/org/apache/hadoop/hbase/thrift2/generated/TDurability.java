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
comment|/**  * Specify Durability:  *  - SKIP_WAL means do not write the Mutation to the WAL.  *  - ASYNC_WAL means write the Mutation to the WAL asynchronously,  *  - SYNC_WAL means write the Mutation to the WAL synchronously,  *  - FSYNC_WAL means Write the Mutation to the WAL synchronously and force the entries to disk.  */
end_comment

begin_enum
specifier|public
enum|enum
name|TDurability
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
block|{
name|SKIP_WAL
argument_list|(
literal|1
argument_list|)
block|,
name|ASYNC_WAL
argument_list|(
literal|2
argument_list|)
block|,
name|SYNC_WAL
argument_list|(
literal|3
argument_list|)
block|,
name|FSYNC_WAL
argument_list|(
literal|4
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
specifier|private
name|TDurability
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
name|TDurability
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
literal|1
case|:
return|return
name|SKIP_WAL
return|;
case|case
literal|2
case|:
return|return
name|ASYNC_WAL
return|;
case|case
literal|3
case|:
return|return
name|SYNC_WAL
return|;
case|case
literal|4
case|:
return|return
name|FSYNC_WAL
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

