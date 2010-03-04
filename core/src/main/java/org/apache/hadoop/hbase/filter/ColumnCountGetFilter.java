begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
import|;
end_import

begin_comment
comment|/**  * Simple filter that returns first N columns on row only.  * This filter was written to test filters in Get and as soon as it gets  * its quota of columns, {@link #filterAllRemaining()} returns true.  This  * makes this filter unsuitable as a Scan filter.  */
end_comment

begin_class
specifier|public
class|class
name|ColumnCountGetFilter
implements|implements
name|Filter
block|{
specifier|private
name|int
name|limit
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|count
init|=
literal|0
decl_stmt|;
comment|/**    * Used during serialization.    * Do not use.    */
specifier|public
name|ColumnCountGetFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ColumnCountGetFilter
parameter_list|(
specifier|final
name|int
name|n
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|n
expr_stmt|;
block|}
specifier|public
name|int
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
name|this
operator|.
name|count
operator|>
name|this
operator|.
name|limit
return|;
block|}
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
name|this
operator|.
name|count
operator|++
expr_stmt|;
return|return
name|filterAllRemaining
argument_list|()
condition|?
name|ReturnCode
operator|.
name|SKIP
else|:
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|count
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|limit
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|limit
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

