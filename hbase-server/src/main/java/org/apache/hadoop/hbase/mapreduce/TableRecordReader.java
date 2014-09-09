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
name|mapreduce
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
name|client
operator|.
name|Scan
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
name|Table
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
name|mapreduce
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
name|mapreduce
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
name|mapreduce
operator|.
name|TaskAttemptContext
import|;
end_import

begin_comment
comment|/**  * Iterate over an HBase table data, return (ImmutableBytesWritable, Result)  * pairs.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|TableRecordReader
extends|extends
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|private
name|TableRecordReaderImpl
name|recordReaderImpl
init|=
operator|new
name|TableRecordReaderImpl
argument_list|()
decl_stmt|;
comment|/**    * Restart from survivable exceptions by creating a new scanner.    *    * @param firstRow  The first row to start at.    * @throws IOException When restarting fails.    */
specifier|public
name|void
name|restart
parameter_list|(
name|byte
index|[]
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|recordReaderImpl
operator|.
name|restart
argument_list|(
name|firstRow
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the HBase table.    *    * @param htable  The {@link HTable} to scan.    */
specifier|public
name|void
name|setHTable
parameter_list|(
name|Table
name|htable
parameter_list|)
block|{
name|this
operator|.
name|recordReaderImpl
operator|.
name|setHTable
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the scan defining the actual details like columns etc.    *    * @param scan  The scan to set.    */
specifier|public
name|void
name|setScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
block|{
name|this
operator|.
name|recordReaderImpl
operator|.
name|setScan
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Closes the split.    *    * @see org.apache.hadoop.mapreduce.RecordReader#close()    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|recordReaderImpl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns the current key.    *    * @return The current key.    * @throws IOException    * @throws InterruptedException When the job is aborted.    * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentKey()    */
annotation|@
name|Override
specifier|public
name|ImmutableBytesWritable
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|this
operator|.
name|recordReaderImpl
operator|.
name|getCurrentKey
argument_list|()
return|;
block|}
comment|/**    * Returns the current value.    *    * @return The current value.    * @throws IOException When the value is faulty.    * @throws InterruptedException When the job is aborted.    * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentValue()    */
annotation|@
name|Override
specifier|public
name|Result
name|getCurrentValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|this
operator|.
name|recordReaderImpl
operator|.
name|getCurrentValue
argument_list|()
return|;
block|}
comment|/**    * Initializes the reader.    *    * @param inputsplit  The split to work with.    * @param context  The current task context.    * @throws IOException When setting up the reader fails.    * @throws InterruptedException When the job is aborted.    * @see org.apache.hadoop.mapreduce.RecordReader#initialize(    *   org.apache.hadoop.mapreduce.InputSplit,    *   org.apache.hadoop.mapreduce.TaskAttemptContext)    */
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|inputsplit
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|this
operator|.
name|recordReaderImpl
operator|.
name|initialize
argument_list|(
name|inputsplit
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**    * Positions the record reader to the next record.    *    * @return<code>true</code> if there was another record.    * @throws IOException When reading the record failed.    * @throws InterruptedException When the job was aborted.    * @see org.apache.hadoop.mapreduce.RecordReader#nextKeyValue()    */
annotation|@
name|Override
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|this
operator|.
name|recordReaderImpl
operator|.
name|nextKeyValue
argument_list|()
return|;
block|}
comment|/**    * The current progress of the record reader through its data.    *    * @return A number between 0.0 and 1.0, the fraction of the data read.    * @see org.apache.hadoop.mapreduce.RecordReader#getProgress()    */
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
return|return
name|this
operator|.
name|recordReaderImpl
operator|.
name|getProgress
argument_list|()
return|;
block|}
block|}
end_class

end_unit

