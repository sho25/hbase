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
name|UnknownScannerException
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
name|hbase
operator|.
name|util
operator|.
name|Writables
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
comment|/**  * Iterate over an HBase table data, return (Text, RowResult) pairs  */
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
name|lastRow
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
name|HTable
name|htable
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|trrInputColumns
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
name|scan
operator|.
name|addColumns
argument_list|(
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
name|scan
operator|.
name|addColumns
argument_list|(
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
name|scan
operator|.
name|addColumns
argument_list|(
name|trrInputColumns
argument_list|)
expr_stmt|;
comment|//      scan.setFilter(trrRowFilter);
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
comment|/**    * @param htable the {@link HTable} to scan.    */
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
catch|catch
parameter_list|(
name|UnknownScannerException
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
name|restart
argument_list|(
name|lastRow
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
name|lastRow
operator|=
name|key
operator|.
name|get
argument_list|()
expr_stmt|;
name|Writables
operator|.
name|copyWritable
argument_list|(
name|result
argument_list|,
name|value
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
block|}
end_class

end_unit

