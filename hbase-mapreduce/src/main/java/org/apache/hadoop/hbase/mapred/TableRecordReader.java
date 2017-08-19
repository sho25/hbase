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
name|yetus
operator|.
name|audience
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
name|mapred
operator|.
name|RecordReader
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
specifier|public
class|class
name|TableRecordReader
implements|implements
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
comment|/**    * Build the scanner. Not done in constructor to allow for extension.    *    * @throws IOException    */
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|recordReaderImpl
operator|.
name|restart
argument_list|(
name|this
operator|.
name|recordReaderImpl
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
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
name|recordReaderImpl
operator|.
name|setInputColumns
argument_list|(
name|inputColumns
argument_list|)
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
name|recordReaderImpl
operator|.
name|setStartRow
argument_list|(
name|startRow
argument_list|)
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
name|recordReaderImpl
operator|.
name|setEndRow
argument_list|(
name|endRow
argument_list|)
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
name|recordReaderImpl
operator|.
name|setRowFilter
argument_list|(
name|rowFilter
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * @return ImmutableBytesWritable    *    * @see org.apache.hadoop.mapred.RecordReader#createKey()    */
specifier|public
name|ImmutableBytesWritable
name|createKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|recordReaderImpl
operator|.
name|createKey
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
name|this
operator|.
name|recordReaderImpl
operator|.
name|createValue
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
name|this
operator|.
name|recordReaderImpl
operator|.
name|getPos
argument_list|()
return|;
block|}
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
comment|// Depends on the total number of tuples and getPos
return|return
name|this
operator|.
name|recordReaderImpl
operator|.
name|getPos
argument_list|()
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
return|return
name|this
operator|.
name|recordReaderImpl
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
block|}
end_class

end_unit

