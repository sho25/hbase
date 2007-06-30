begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|fs
operator|.
name|Path
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
name|io
operator|.
name|Text
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
name|JobConfigurable
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
name|HClient
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
name|HScannerInterface
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
name|HStoreKey
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
name|KeyedData
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
name|KeyedDataArrayWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * Convert HBase tabular data into a format that is consumable by Map/Reduce  */
end_comment

begin_class
specifier|public
class|class
name|TableInputFormat
implements|implements
name|InputFormat
implements|,
name|JobConfigurable
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|TableInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * space delimited list of columns     * @see org.apache.hadoop.hbase.HAbstractScanner for column name wildcards    */
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN_LIST
init|=
literal|"hbase.mapred.tablecolumns"
decl_stmt|;
specifier|private
name|Text
name|m_tableName
decl_stmt|;
name|Text
index|[]
name|m_cols
decl_stmt|;
name|HClient
name|m_client
decl_stmt|;
comment|/**    * Iterate over an HBase table data, return (HStoreKey, KeyedDataArrayWritable) pairs    */
class|class
name|TableRecordReader
implements|implements
name|RecordReader
block|{
specifier|private
name|HScannerInterface
name|m_scanner
decl_stmt|;
specifier|private
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|m_row
decl_stmt|;
comment|// current buffer
specifier|private
name|Text
name|m_endRow
decl_stmt|;
comment|/**      * Constructor      * @param startRow (inclusive)      * @param endRow (exclusive)      * @throws IOException      */
specifier|public
name|TableRecordReader
parameter_list|(
name|Text
name|startRow
parameter_list|,
name|Text
name|endRow
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"start construct"
argument_list|)
expr_stmt|;
name|m_row
operator|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|m_scanner
operator|=
name|m_client
operator|.
name|obtainScanner
argument_list|(
name|m_cols
argument_list|,
name|startRow
argument_list|)
expr_stmt|;
name|m_endRow
operator|=
name|endRow
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"end construct"
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)      * @see org.apache.hadoop.mapred.RecordReader#close()      */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"start close"
argument_list|)
expr_stmt|;
name|m_scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"end close"
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return HStoreKey      *      * @see org.apache.hadoop.mapred.RecordReader#createKey()      */
specifier|public
name|WritableComparable
name|createKey
parameter_list|()
block|{
return|return
operator|new
name|HStoreKey
argument_list|()
return|;
block|}
comment|/**      * @return KeyedDataArrayWritable of KeyedData      *      * @see org.apache.hadoop.mapred.RecordReader#createValue()      */
specifier|public
name|Writable
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|KeyedDataArrayWritable
argument_list|()
return|;
block|}
comment|/* (non-Javadoc)      * @see org.apache.hadoop.mapred.RecordReader#getPos()      */
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
comment|/* (non-Javadoc)      * @see org.apache.hadoop.mapred.RecordReader#getProgress()      */
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
comment|/**      * @param key HStoreKey as input key.      * @param value KeyedDataArrayWritable as input value      *       * Converts HScannerInterface.next(HStoreKey, TreeMap(Text, byte[])) to      *                                (HStoreKey, KeyedDataArrayWritable)      * @return true if there was more data      * @throws IOException      */
specifier|public
name|boolean
name|next
parameter_list|(
name|Writable
name|key
parameter_list|,
name|Writable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"start next"
argument_list|)
expr_stmt|;
name|m_row
operator|.
name|clear
argument_list|()
expr_stmt|;
name|HStoreKey
name|tKey
init|=
operator|(
name|HStoreKey
operator|)
name|key
decl_stmt|;
name|boolean
name|hasMore
init|=
name|m_scanner
operator|.
name|next
argument_list|(
name|tKey
argument_list|,
name|m_row
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasMore
condition|)
block|{
if|if
condition|(
name|m_endRow
operator|.
name|getLength
argument_list|()
operator|>
literal|0
operator|&&
operator|(
name|tKey
operator|.
name|getRow
argument_list|()
operator|.
name|compareTo
argument_list|(
name|m_endRow
argument_list|)
operator|<
literal|0
operator|)
condition|)
block|{
name|hasMore
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|KeyedDataArrayWritable
name|rowVal
init|=
operator|(
name|KeyedDataArrayWritable
operator|)
name|value
decl_stmt|;
name|ArrayList
argument_list|<
name|KeyedData
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyedData
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|m_row
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|HStoreKey
name|keyCol
init|=
operator|new
name|HStoreKey
argument_list|(
name|tKey
argument_list|)
decl_stmt|;
name|keyCol
operator|.
name|setColumn
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
operator|new
name|KeyedData
argument_list|(
name|keyCol
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// set the output
name|rowVal
operator|.
name|set
argument_list|(
name|columns
operator|.
name|toArray
argument_list|(
operator|new
name|KeyedData
index|[
name|columns
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"end next"
argument_list|)
expr_stmt|;
return|return
name|hasMore
return|;
block|}
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.mapred.InputFormat#getRecordReader(org.apache.hadoop.mapred.InputSplit, org.apache.hadoop.mapred.JobConf, org.apache.hadoop.mapred.Reporter)    */
specifier|public
name|RecordReader
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|JobConf
name|job
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
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
return|return
operator|new
name|TableRecordReader
argument_list|(
name|tSplit
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|tSplit
operator|.
name|getEndRow
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * A split will be created for each HRegion of the input table    *    * @see org.apache.hadoop.mapred.InputFormat#getSplits(org.apache.hadoop.mapred.JobConf, int)    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"start getSplits"
argument_list|)
expr_stmt|;
name|Text
index|[]
name|startKeys
init|=
name|m_client
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
name|InputSplit
index|[]
name|splits
init|=
operator|new
name|InputSplit
index|[
name|startKeys
operator|.
name|length
index|]
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
name|startKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|splits
index|[
name|i
index|]
operator|=
operator|new
name|TableSplit
argument_list|(
name|m_tableName
argument_list|,
name|startKeys
index|[
name|i
index|]
argument_list|,
operator|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|<
name|startKeys
operator|.
name|length
operator|)
condition|?
name|startKeys
index|[
name|i
operator|+
literal|1
index|]
else|:
operator|new
name|Text
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
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
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"end splits"
argument_list|)
expr_stmt|;
return|return
name|splits
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.mapred.JobConfigurable#configure(org.apache.hadoop.mapred.JobConf)    */
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"start configure"
argument_list|)
expr_stmt|;
name|Path
index|[]
name|tableNames
init|=
name|job
operator|.
name|getInputPaths
argument_list|()
decl_stmt|;
name|m_tableName
operator|=
operator|new
name|Text
argument_list|(
name|tableNames
index|[
literal|0
index|]
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|colArg
init|=
name|job
operator|.
name|get
argument_list|(
name|COLUMN_LIST
argument_list|)
decl_stmt|;
name|String
index|[]
name|colNames
init|=
name|colArg
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
name|m_cols
operator|=
operator|new
name|Text
index|[
name|colNames
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|m_cols
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|m_cols
index|[
name|i
index|]
operator|=
operator|new
name|Text
argument_list|(
name|colNames
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|m_client
operator|=
operator|new
name|HClient
argument_list|(
name|job
argument_list|)
expr_stmt|;
try|try
block|{
name|m_client
operator|.
name|openTable
argument_list|(
name|m_tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"end configure"
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.mapred.InputFormat#validateInput(org.apache.hadoop.mapred.JobConf)    */
specifier|public
name|void
name|validateInput
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// expecting exactly one path
name|Path
index|[]
name|tableNames
init|=
name|job
operator|.
name|getInputPaths
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableNames
operator|==
literal|null
operator|||
name|tableNames
operator|.
name|length
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"expecting one table name"
argument_list|)
throw|;
block|}
comment|// expecting at least one column
name|String
name|colArg
init|=
name|job
operator|.
name|get
argument_list|(
name|COLUMN_LIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|colArg
operator|==
literal|null
operator|||
name|colArg
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"expecting at least one column"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

