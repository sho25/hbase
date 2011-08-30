begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|DoNotRetryIOException
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
name|ResultScanner
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Iterate over an HBase table data, return (ImmutableBytesWritable, Result)  * pairs.  */
end_comment

begin_class
specifier|public
class|class
name|TableRecordReaderImpl
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ResultScanner
name|scanner
init|=
literal|null
decl_stmt|;
specifier|private
name|Scan
name|scan
init|=
literal|null
decl_stmt|;
specifier|private
name|HTable
name|htable
init|=
literal|null
decl_stmt|;
specifier|private
name|byte
index|[]
name|lastSuccessfulRow
init|=
literal|null
decl_stmt|;
specifier|private
name|ImmutableBytesWritable
name|key
init|=
literal|null
decl_stmt|;
specifier|private
name|Result
name|value
init|=
literal|null
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
name|Scan
name|newScan
init|=
operator|new
name|Scan
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|newScan
operator|.
name|setStartRow
argument_list|(
name|firstRow
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanner
operator|=
name|this
operator|.
name|htable
operator|.
name|getScanner
argument_list|(
name|newScan
argument_list|)
expr_stmt|;
block|}
comment|/**    * Build the scanner. Not done in constructor to allow for extension.    *    * @throws IOException When restarting the scan fails.    */
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|restart
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Sets the HBase table.    *    * @param htable  The {@link HTable} to scan.    */
specifier|public
name|void
name|setHTable
parameter_list|(
name|HTable
name|htable
parameter_list|)
block|{
name|this
operator|.
name|htable
operator|=
name|htable
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
name|scan
operator|=
name|scan
expr_stmt|;
block|}
comment|/**    * Closes the split.    *    *    */
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns the current key.    *    * @return The current key.    * @throws IOException    * @throws InterruptedException When the job is aborted.    */
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
name|key
return|;
block|}
comment|/**    * Returns the current value.    *    * @return The current value.    * @throws IOException When the value is faulty.    * @throws InterruptedException When the job is aborted.    */
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
name|value
return|;
block|}
comment|/**    * Positions the record reader to the next record.    *    * @return<code>true</code> if there was another record.    * @throws IOException When reading the record failed.    * @throws InterruptedException When the job was aborted.    */
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
name|key
operator|=
operator|new
name|ImmutableBytesWritable
argument_list|()
expr_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
name|value
operator|=
operator|new
name|Result
argument_list|()
expr_stmt|;
try|try
block|{
name|value
operator|=
name|this
operator|.
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DoNotRetryIOException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"recovered from "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastSuccessfulRow
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"We are restarting the first next() invocation,"
operator|+
literal|" if your mapper's restarted a few other times like this"
operator|+
literal|" then you should consider killing this job and investigate"
operator|+
literal|" why it's taking so long."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastSuccessfulRow
operator|==
literal|null
condition|)
block|{
name|restart
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|restart
argument_list|(
name|lastSuccessfulRow
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// skip presumed already mapped row
block|}
name|value
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|!=
literal|null
operator|&&
name|value
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|key
operator|.
name|set
argument_list|(
name|value
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|lastSuccessfulRow
operator|=
name|key
operator|.
name|get
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * The current progress of the record reader through its data.    *    * @return A number between 0.0 and 1.0, the fraction of the data read.    */
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
comment|// Depends on the total number of tuples
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

