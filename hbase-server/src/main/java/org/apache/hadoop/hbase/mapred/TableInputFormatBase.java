begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
package|;
end_package

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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|HConstants
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|Result
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
name|filter
operator|.
name|Filter
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
name|hbase
operator|.
name|regionserver
operator|.
name|HRegion
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
name|InputFormat
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
name|InputSplit
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
name|RecordReader
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
name|Reporter
import|;
end_import

begin_comment
comment|/**  * A Base for {@link TableInputFormat}s. Receives a {@link HTable}, a  * byte[] of input columns and optionally a {@link Filter}.  * Subclasses may use other TableRecordReader implementations.  *<p>  * An example of a subclass:  *<pre>  *   class ExampleTIF extends TableInputFormatBase implements JobConfigurable {  *  *     public void configure(JobConf job) {  *       HTable exampleTable = new HTable(HBaseConfiguration.create(job),  *         Bytes.toBytes("exampleTable"));  *       // mandatory  *       setHTable(exampleTable);  *       Text[] inputColumns = new byte [][] { Bytes.toBytes("columnA"),  *         Bytes.toBytes("columnB") };  *       // mandatory  *       setInputColumns(inputColumns);  *       RowFilterInterface exampleFilter = new RegExpRowFilter("keyPrefix.*");  *       // optional  *       setRowFilter(exampleFilter);  *     }  *  *     public void validateInput(JobConf job) throws IOException {  *     }  *  }  *</pre>  */
end_comment

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|abstract
class|class
name|TableInputFormatBase
implements|implements
name|InputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableInputFormatBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|inputColumns
decl_stmt|;
specifier|private
name|HTable
name|table
decl_stmt|;
specifier|private
name|TableRecordReader
name|tableRecordReader
decl_stmt|;
specifier|private
name|Filter
name|rowFilter
decl_stmt|;
comment|/**    * Builds a TableRecordReader. If no TableRecordReader was provided, uses    * the default.    *    * @see org.apache.hadoop.mapred.InputFormat#getRecordReader(InputSplit,    *      JobConf, Reporter)    */
specifier|public
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSplit
name|tSplit
init|=
operator|(
name|TableSplit
operator|)
name|split
decl_stmt|;
name|TableRecordReader
name|trr
init|=
name|this
operator|.
name|tableRecordReader
decl_stmt|;
comment|// if no table record reader was provided use default
if|if
condition|(
name|trr
operator|==
literal|null
condition|)
block|{
name|trr
operator|=
operator|new
name|TableRecordReader
argument_list|()
expr_stmt|;
block|}
name|trr
operator|.
name|setStartRow
argument_list|(
name|tSplit
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setEndRow
argument_list|(
name|tSplit
operator|.
name|getEndRow
argument_list|()
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setHTable
argument_list|(
name|this
operator|.
name|table
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setInputColumns
argument_list|(
name|this
operator|.
name|inputColumns
argument_list|)
expr_stmt|;
name|trr
operator|.
name|setRowFilter
argument_list|(
name|this
operator|.
name|rowFilter
argument_list|)
expr_stmt|;
name|trr
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|trr
return|;
block|}
comment|/**    * Calculates the splits that will serve as input for the map tasks.    *<ul>    * Splits are created in number equal to the smallest between numSplits and    * the number of {@link HRegion}s in the table. If the number of splits is    * smaller than the number of {@link HRegion}s then splits are spanned across    * multiple {@link HRegion}s and are grouped the most evenly possible. In the    * case splits are uneven the bigger splits are placed first in the    * {@link InputSplit} array.    *    * @param job the map task {@link JobConf}    * @param numSplits a hint to calculate the number of splits (mapred.map.tasks).    *    * @return the input splits    *    * @see org.apache.hadoop.mapred.InputFormat#getSplits(org.apache.hadoop.mapred.JobConf, int)    */
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|table
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No table was provided"
argument_list|)
throw|;
block|}
name|byte
index|[]
index|[]
name|startKeys
init|=
name|this
operator|.
name|table
operator|.
name|getStartKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|startKeys
operator|==
literal|null
operator|||
name|startKeys
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expecting at least one region"
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|.
name|inputColumns
operator|==
literal|null
operator|||
name|this
operator|.
name|inputColumns
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expecting at least one column"
argument_list|)
throw|;
block|}
name|int
name|realNumSplits
init|=
name|numSplits
operator|>
name|startKeys
operator|.
name|length
condition|?
name|startKeys
operator|.
name|length
else|:
name|numSplits
decl_stmt|;
name|InputSplit
index|[]
name|splits
init|=
operator|new
name|InputSplit
index|[
name|realNumSplits
index|]
decl_stmt|;
name|int
name|middle
init|=
name|startKeys
operator|.
name|length
operator|/
name|realNumSplits
decl_stmt|;
name|int
name|startPos
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|realNumSplits
condition|;
name|i
operator|++
control|)
block|{
name|int
name|lastPos
init|=
name|startPos
operator|+
name|middle
decl_stmt|;
name|lastPos
operator|=
name|startKeys
operator|.
name|length
operator|%
name|realNumSplits
operator|>
name|i
condition|?
name|lastPos
operator|+
literal|1
else|:
name|lastPos
expr_stmt|;
name|String
name|regionLocation
init|=
name|table
operator|.
name|getRegionLocation
argument_list|(
name|startKeys
index|[
name|startPos
index|]
argument_list|)
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|splits
index|[
name|i
index|]
operator|=
operator|new
name|TableSplit
argument_list|(
name|this
operator|.
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|startKeys
index|[
name|startPos
index|]
argument_list|,
operator|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|<
name|realNumSplits
operator|)
condition|?
name|startKeys
index|[
name|lastPos
index|]
else|:
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|regionLocation
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"split: "
operator|+
name|i
operator|+
literal|"->"
operator|+
name|splits
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|startPos
operator|=
name|lastPos
expr_stmt|;
block|}
return|return
name|splits
return|;
block|}
comment|/**    * @param inputColumns to be passed in {@link Result} to the map task.    */
specifier|protected
name|void
name|setInputColumns
parameter_list|(
name|byte
index|[]
index|[]
name|inputColumns
parameter_list|)
block|{
name|this
operator|.
name|inputColumns
operator|=
name|inputColumns
expr_stmt|;
block|}
comment|/**    * Allows subclasses to get the {@link HTable}.    */
specifier|protected
name|HTable
name|getHTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
return|;
block|}
comment|/**    * Allows subclasses to set the {@link HTable}.    *    * @param table to get the data from    */
specifier|protected
name|void
name|setHTable
parameter_list|(
name|HTable
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
comment|/**    * Allows subclasses to set the {@link TableRecordReader}.    *    * @param tableRecordReader    *                to provide other {@link TableRecordReader} implementations.    */
specifier|protected
name|void
name|setTableRecordReader
parameter_list|(
name|TableRecordReader
name|tableRecordReader
parameter_list|)
block|{
name|this
operator|.
name|tableRecordReader
operator|=
name|tableRecordReader
expr_stmt|;
block|}
comment|/**    * Allows subclasses to set the {@link Filter} to be used.    *    * @param rowFilter    */
specifier|protected
name|void
name|setRowFilter
parameter_list|(
name|Filter
name|rowFilter
parameter_list|)
block|{
name|this
operator|.
name|rowFilter
operator|=
name|rowFilter
expr_stmt|;
block|}
block|}
end_class

end_unit

