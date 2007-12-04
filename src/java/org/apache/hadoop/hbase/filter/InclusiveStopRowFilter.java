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
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/*  * Subclass of StopRowFilter that filters rows> the stop row,  * making it include up to the last row but no further.  */
end_comment

begin_class
specifier|public
class|class
name|InclusiveStopRowFilter
extends|extends
name|StopRowFilter
block|{
comment|/**    * Default constructor, filters nothing. Required though for RPC    * deserialization.    */
specifier|public
name|InclusiveStopRowFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that takes a stopRowKey on which to filter    *     * @param stopRowKey rowKey to filter on.    */
specifier|public
name|InclusiveStopRowFilter
parameter_list|(
specifier|final
name|Text
name|stopRowKey
parameter_list|)
block|{
name|super
argument_list|(
name|stopRowKey
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|filter
parameter_list|(
specifier|final
name|Text
name|rowKey
parameter_list|)
block|{
if|if
condition|(
name|rowKey
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|stopRowKey
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
name|boolean
name|result
init|=
name|this
operator|.
name|stopRowKey
operator|.
name|compareTo
argument_list|(
name|rowKey
argument_list|)
operator|<
literal|0
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter result for rowKey: "
operator|+
name|rowKey
operator|+
literal|".  Result: "
operator|+
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

