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
name|hbase
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
name|conf
operator|.
name|Configuration
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
name|client
operator|.
name|ScannerCallable
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
name|mapreduce
operator|.
name|TableInputFormat
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
name|util
operator|.
name|Bytes
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

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapreduce
operator|.
name|TableRecordReaderImpl
operator|.
name|LOG_PER_ROW_COUNT
import|;
end_import

begin_comment
comment|/**  * Iterate over an HBase table data, return (Text, RowResult) pairs  */
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
name|TableRecordReaderImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|startRow
decl_stmt|;
specifier|private
name|byte
index|[]
name|endRow
decl_stmt|;
specifier|private
name|byte
index|[]
name|lastSuccessfulRow
decl_stmt|;
specifier|private
name|Filter
name|trrRowFilter
decl_stmt|;
specifier|private
name|ResultScanner
name|scanner
decl_stmt|;
specifier|private
name|Table
name|htable
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|trrInputColumns
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|private
name|int
name|rowcount
decl_stmt|;
specifier|private
name|boolean
name|logScannerActivity
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|logPerRowCount
init|=
literal|100
decl_stmt|;
comment|/**    * Restart from survivable exceptions by creating a new scanner.    *    * @param firstRow    * @throws IOException    */
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
name|currentScan
decl_stmt|;
if|if
condition|(
operator|(
name|endRow
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|endRow
operator|.
name|length
operator|>
literal|0
operator|)
condition|)
block|{
if|if
condition|(
name|trrRowFilter
operator|!=
literal|null
condition|)
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|firstRow
argument_list|,
name|endRow
argument_list|)
decl_stmt|;
name|TableInputFormat
operator|.
name|addColumns
argument_list|(
name|scan
argument_list|,
name|trrInputColumns
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|trrRowFilter
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
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
name|scan
argument_list|)
expr_stmt|;
name|currentScan
operator|=
name|scan
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"TIFB.restart, firstRow: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|firstRow
argument_list|)
operator|+
literal|", endRow: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|endRow
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|firstRow
argument_list|,
name|endRow
argument_list|)
decl_stmt|;
name|TableInputFormat
operator|.
name|addColumns
argument_list|(
name|scan
argument_list|,
name|trrInputColumns
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
name|scan
argument_list|)
expr_stmt|;
name|currentScan
operator|=
name|scan
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"TIFB.restart, firstRow: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|firstRow
argument_list|)
operator|+
literal|", no endRow"
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|firstRow
argument_list|)
decl_stmt|;
name|TableInputFormat
operator|.
name|addColumns
argument_list|(
name|scan
argument_list|,
name|trrInputColumns
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|trrRowFilter
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
name|scan
argument_list|)
expr_stmt|;
name|currentScan
operator|=
name|scan
expr_stmt|;
block|}
if|if
condition|(
name|logScannerActivity
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Current scan="
operator|+
name|currentScan
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|timestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|rowcount
operator|=
literal|0
expr_stmt|;
block|}
block|}
comment|/**    * Build the scanner. Not done in constructor to allow for extension.    *    * @throws IOException    */
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|restart
argument_list|(
name|startRow
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|getStartRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|startRow
return|;
block|}
comment|/**    * @param htable the {@link org.apache.hadoop.hbase.HTableDescriptor} to scan.    */
specifier|public
name|void
name|setHTable
parameter_list|(
name|Table
name|htable
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|htable
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|logScannerActivity
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|ScannerCallable
operator|.
name|LOG_SCANNER_ACTIVITY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|logPerRowCount
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|LOG_PER_ROW_COUNT
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|this
operator|.
name|htable
operator|=
name|htable
expr_stmt|;
block|}
comment|/**    * @param inputColumns the columns to be placed in {@link Result}.    */
specifier|public
name|void
name|setInputColumns
parameter_list|(
specifier|final
name|byte
index|[]
index|[]
name|inputColumns
parameter_list|)
block|{
name|this
operator|.
name|trrInputColumns
operator|=
name|inputColumns
expr_stmt|;
block|}
comment|/**    * @param startRow the first row in the split    */
specifier|public
name|void
name|setStartRow
parameter_list|(
specifier|final
name|byte
index|[]
name|startRow
parameter_list|)
block|{
name|this
operator|.
name|startRow
operator|=
name|startRow
expr_stmt|;
block|}
comment|/**    *    * @param endRow the last row in the split    */
specifier|public
name|void
name|setEndRow
parameter_list|(
specifier|final
name|byte
index|[]
name|endRow
parameter_list|)
block|{
name|this
operator|.
name|endRow
operator|=
name|endRow
expr_stmt|;
block|}
comment|/**    * @param rowFilter the {@link Filter} to be used.    */
specifier|public
name|void
name|setRowFilter
parameter_list|(
name|Filter
name|rowFilter
parameter_list|)
block|{
name|this
operator|.
name|trrRowFilter
operator|=
name|rowFilter
expr_stmt|;
block|}
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
try|try
block|{
name|this
operator|.
name|htable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error closing table"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return ImmutableBytesWritable    *    * @see org.apache.hadoop.mapred.RecordReader#createKey()    */
specifier|public
name|ImmutableBytesWritable
name|createKey
parameter_list|()
block|{
return|return
operator|new
name|ImmutableBytesWritable
argument_list|()
return|;
block|}
comment|/**    * @return RowResult    *    * @see org.apache.hadoop.mapred.RecordReader#createValue()    */
specifier|public
name|Result
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|Result
argument_list|()
return|;
block|}
specifier|public
name|long
name|getPos
parameter_list|()
block|{
comment|// This should be the ordinal tuple in the range;
comment|// not clear how to calculate...
return|return
literal|0
return|;
block|}
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
comment|// Depends on the total number of tuples and getPos
return|return
literal|0
return|;
block|}
comment|/**    * @param key HStoreKey as input key.    * @param value MapWritable as input value    * @return true if there was more data    * @throws IOException    */
specifier|public
name|boolean
name|next
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Result
name|result
decl_stmt|;
try|try
block|{
try|try
block|{
name|result
operator|=
name|this
operator|.
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|logScannerActivity
condition|)
block|{
name|rowcount
operator|++
expr_stmt|;
if|if
condition|(
name|rowcount
operator|>=
name|logPerRowCount
condition|)
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mapper took "
operator|+
operator|(
name|now
operator|-
name|timestamp
operator|)
operator|+
literal|"ms to process "
operator|+
name|rowcount
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|timestamp
operator|=
name|now
expr_stmt|;
name|rowcount
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// try to handle all IOExceptions by restarting
comment|// the scanner, if the second call fails, it will be rethrown
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
literal|" if your mapper has restarted a few other times like this"
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
name|startRow
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
name|this
operator|.
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
comment|// skip presumed already mapped row
block|}
name|result
operator|=
name|this
operator|.
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
name|result
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
name|result
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
name|value
operator|.
name|copyFrom
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
if|if
condition|(
name|logScannerActivity
condition|)
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Mapper took "
operator|+
operator|(
name|now
operator|-
name|timestamp
operator|)
operator|+
literal|"ms to process "
operator|+
name|rowcount
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|ioe
argument_list|)
expr_stmt|;
name|String
name|lastRow
init|=
name|lastSuccessfulRow
operator|==
literal|null
condition|?
literal|"null"
else|:
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|lastSuccessfulRow
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"lastSuccessfulRow="
operator|+
name|lastRow
argument_list|)
expr_stmt|;
block|}
throw|throw
name|ioe
throw|;
block|}
block|}
block|}
end_class

end_unit

