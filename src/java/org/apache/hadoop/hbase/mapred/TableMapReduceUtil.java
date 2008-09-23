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
name|mapred
package|;
end_package

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
name|io
operator|.
name|BatchUpdate
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|Writable
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
name|WritableComparable
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
name|mapred
operator|.
name|FileInputFormat
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
class|class
name|TableMapReduceUtil
block|{
comment|/**    * Use this before submitting a TableMap job. It will    * appropriately set up the JobConf.    *     * @param table table name    * @param columns columns to scan    * @param mapper mapper class    * @param outputKeyClass    * @param outputValueClass    * @param job job configuration    */
specifier|public
specifier|static
name|void
name|initTableMapJob
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columns
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableMap
argument_list|>
name|mapper
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|WritableComparable
argument_list|>
name|outputKeyClass
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|outputValueClass
parameter_list|,
name|JobConf
name|job
parameter_list|)
block|{
name|job
operator|.
name|setInputFormat
argument_list|(
name|TableInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|outputValueClass
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|outputKeyClass
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|addInputPaths
argument_list|(
name|job
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|TableInputFormat
operator|.
name|COLUMN_LIST
argument_list|,
name|columns
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use this before submitting a TableReduce job. It will    * appropriately set up the JobConf.    *     * @param table    * @param reducer    * @param job    */
specifier|public
specifier|static
name|void
name|initTableReduceJob
parameter_list|(
name|String
name|table
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TableReduce
argument_list|>
name|reducer
parameter_list|,
name|JobConf
name|job
parameter_list|)
block|{
name|job
operator|.
name|setOutputFormat
argument_list|(
name|TableOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|reducer
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|TableOutputFormat
operator|.
name|OUTPUT_TABLE
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|BatchUpdate
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

