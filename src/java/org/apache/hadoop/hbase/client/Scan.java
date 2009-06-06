begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|KeyValue
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
name|filter
operator|.
name|RowFilterInterface
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
name|HbaseObjectWritable
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
name|TimeRange
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
name|io
operator|.
name|Writable
import|;
end_import

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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
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
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * Used to perform Scan operations.  *<p>  * All operations are identical to {@link Get} with the exception of  * instantiation.  Rather than specifying a single row, an optional startRow  * and stopRow may be defined.  If rows are not specified, the Scanner will  * iterate over all rows.  *<p>  * To scan everything for each row, instantiate a Scan object.  * To further define the scope of what to get when scanning, perform additional   * methods as outlined below.  *<p>  * To get all columns from specific families, execute {@link #addFamily(byte[]) addFamily}  * for each family to retrieve.  *<p>  * To get specific columns, execute {@link #addColumn(byte[], byte[]) addColumn}  * for each column to retrieve.  *<p>  * To only retrieve columns within a specific range of version timestamps,  * execute {@link #setTimeRange(long, long) setTimeRange}.  *<p>  * To only retrieve columns with a specific timestamp, execute  * {@link #setTimeStamp(long) setTimestamp}.  *<p>  * To limit the number of versions of each column to be returned, execute  * {@link #setMaxVersions(int) setMaxVersions}.  *<p>  * To add a filter, execute {@link #setFilter(org.apache.hadoop.hbase.filter.Filter) setFilter}.  */
end_comment

begin_class
specifier|public
class|class
name|Scan
implements|implements
name|Writable
block|{
specifier|private
name|byte
index|[]
name|startRow
init|=
name|HConstants
operator|.
name|EMPTY_START_ROW
decl_stmt|;
specifier|private
name|byte
index|[]
name|stopRow
init|=
name|HConstants
operator|.
name|EMPTY_END_ROW
decl_stmt|;
specifier|private
name|int
name|maxVersions
init|=
literal|1
decl_stmt|;
specifier|private
name|Filter
name|filter
init|=
literal|null
decl_stmt|;
specifier|private
name|RowFilterInterface
name|oldFilter
init|=
literal|null
decl_stmt|;
specifier|private
name|TimeRange
name|tr
init|=
operator|new
name|TimeRange
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|familyMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * Create a Scan operation across all rows.    */
specifier|public
name|Scan
parameter_list|()
block|{}
specifier|public
name|Scan
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|Filter
name|filter
parameter_list|)
block|{
name|this
argument_list|(
name|startRow
argument_list|)
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
comment|/**    * Create a Scan operation starting at the specified row.    *<p>    * If the specified row does not exist, the Scanner will start from the    * next closest row after the specified row.    * @param startRow row to start scanner at or after    */
specifier|public
name|Scan
parameter_list|(
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
comment|/**    * Create a Scan operation for the range of rows specified.    * @param startRow row to start scanner at or after (inclusive)    * @param stopRow row to stop scanner before (exclusive)    */
specifier|public
name|Scan
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|)
block|{
name|this
operator|.
name|startRow
operator|=
name|startRow
expr_stmt|;
name|this
operator|.
name|stopRow
operator|=
name|stopRow
expr_stmt|;
block|}
comment|/**    * Get all columns from the specified family.    *<p>    * Overrides previous calls to addColumn for this family.    * @param family family name    */
specifier|public
name|Scan
name|addFamily
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
name|familyMap
operator|.
name|remove
argument_list|(
name|family
argument_list|)
expr_stmt|;
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get the column from the specified family with the specified qualifier.    *<p>    * Overrides previous calls to addFamily for this family.    * @param family family name    * @param qualifier column qualifier    */
specifier|public
name|Scan
name|addColumn
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
name|familyMap
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|set
operator|==
literal|null
condition|)
block|{
name|set
operator|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
expr_stmt|;
block|}
name|set
operator|.
name|add
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|set
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Adds an array of columns specified the old format, family:qualifier.    *<p>    * Overrides previous calls to addFamily for any families in the input.    * @param columns array of columns, formatted as<pre>family:qualifier</pre>    */
specifier|public
name|Scan
name|addColumns
parameter_list|(
name|byte
index|[]
index|[]
name|columns
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|byte
index|[]
index|[]
name|split
init|=
name|KeyValue
operator|.
name|parseColumn
argument_list|(
name|columns
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|addColumn
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|split
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{}
block|}
return|return
name|this
return|;
block|}
comment|/**    * Get versions of columns only within the specified timestamp range,    * [minStamp, maxStamp).    * @param minStamp minimum timestamp value, inclusive    * @param maxStamp maximum timestamp value, exclusive    * @throws IOException if invalid time range    */
specifier|public
name|Scan
name|setTimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
throws|throws
name|IOException
block|{
name|tr
operator|=
operator|new
name|TimeRange
argument_list|(
name|minStamp
argument_list|,
name|maxStamp
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get versions of columns with the specified timestamp.    * @param timestamp version timestamp      */
specifier|public
name|Scan
name|setTimeStamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
try|try
block|{
name|tr
operator|=
operator|new
name|TimeRange
argument_list|(
name|timestamp
argument_list|,
name|timestamp
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Will never happen
block|}
return|return
name|this
return|;
block|}
comment|/**    * Set the start row.    * @param startRow    */
specifier|public
name|Scan
name|setStartRow
parameter_list|(
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
return|return
name|this
return|;
block|}
comment|/**    * Set the stop row.    * @param stopRow    */
specifier|public
name|Scan
name|setStopRow
parameter_list|(
name|byte
index|[]
name|stopRow
parameter_list|)
block|{
name|this
operator|.
name|stopRow
operator|=
name|stopRow
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get all available versions.    */
specifier|public
name|Scan
name|setMaxVersions
parameter_list|()
block|{
name|this
operator|.
name|maxVersions
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Get up to the specified number of versions of each column.    * @param maxVersions maximum versions for each column    * @throws IOException if invalid number of versions    */
specifier|public
name|Scan
name|setMaxVersions
parameter_list|(
name|int
name|maxVersions
parameter_list|)
block|{
name|this
operator|.
name|maxVersions
operator|=
name|maxVersions
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Apply the specified server-side filter when performing the Scan.    * @param filter filter to run on the server    */
specifier|public
name|Scan
name|setFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Set an old-style filter interface to use. Note: not all features of the    * old style filters are supported.    * @deprecated    * @param filter    * @return    */
specifier|public
name|Scan
name|setOldFilter
parameter_list|(
name|RowFilterInterface
name|filter
parameter_list|)
block|{
name|oldFilter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Setting the familyMap    * @param familyMap    */
specifier|public
name|Scan
name|setFamilyMap
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|familyMap
parameter_list|)
block|{
name|this
operator|.
name|familyMap
operator|=
name|familyMap
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Getting the familyMap    * @return familyMap    */
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|getFamilyMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|familyMap
return|;
block|}
comment|/**    * @return the number of families in familyMap    */
specifier|public
name|int
name|numFamilies
parameter_list|()
block|{
if|if
condition|(
name|hasFamilies
argument_list|()
condition|)
block|{
return|return
name|this
operator|.
name|familyMap
operator|.
name|size
argument_list|()
return|;
block|}
return|return
literal|0
return|;
block|}
comment|/**    * @return true if familyMap is non empty, false otherwise    */
specifier|public
name|boolean
name|hasFamilies
parameter_list|()
block|{
return|return
operator|!
name|this
operator|.
name|familyMap
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * @return the keys of the familyMap    */
specifier|public
name|byte
index|[]
index|[]
name|getFamilies
parameter_list|()
block|{
if|if
condition|(
name|hasFamilies
argument_list|()
condition|)
block|{
return|return
name|this
operator|.
name|familyMap
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
index|[
literal|0
index|]
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @return the startrow    */
specifier|public
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
comment|/**    * @return the stoprow    */
specifier|public
name|byte
index|[]
name|getStopRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopRow
return|;
block|}
comment|/**    * @return the max number of versions to fetch    */
specifier|public
name|int
name|getMaxVersions
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxVersions
return|;
block|}
comment|/**    * @return TimeRange    */
specifier|public
name|TimeRange
name|getTimeRange
parameter_list|()
block|{
return|return
name|this
operator|.
name|tr
return|;
block|}
comment|/**    * @return RowFilter    */
specifier|public
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
comment|/**    * Get the old style filter, if there is one.    * @deprecated    * @return null or instance    */
specifier|public
name|RowFilterInterface
name|getOldFilter
parameter_list|()
block|{
return|return
name|oldFilter
return|;
block|}
comment|/**    * @return true is a filter has been specified, false if not    */
specifier|public
name|boolean
name|hasFilter
parameter_list|()
block|{
return|return
name|filter
operator|!=
literal|null
operator|||
name|oldFilter
operator|!=
literal|null
return|;
block|}
comment|/**    * @return String    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"startRow="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|startRow
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", stopRow="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|stopRow
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", maxVersions="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|""
operator|+
name|this
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", timeRange="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"["
operator|+
name|this
operator|.
name|tr
operator|.
name|getMin
argument_list|()
operator|+
literal|","
operator|+
name|this
operator|.
name|tr
operator|.
name|getMax
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", families="
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|familyMap
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"ALL"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
name|boolean
name|moreThanOne
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|this
operator|.
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|moreThanOne
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"), "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|moreThanOne
operator|=
literal|true
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"(family="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", columns="
argument_list|)
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"ALL"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|boolean
name|moreThanOneB
init|=
literal|false
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
name|moreThanOneB
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|moreThanOneB
operator|=
literal|true
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|//Writable
specifier|public
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|startRow
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopRow
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxVersions
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|boolean
name|hasFilter
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasFilter
condition|)
block|{
name|this
operator|.
name|filter
operator|=
operator|(
name|Filter
operator|)
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|boolean
name|hasOldFilter
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasOldFilter
condition|)
block|{
name|this
operator|.
name|oldFilter
operator|=
operator|(
name|RowFilterInterface
operator|)
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|tr
operator|=
operator|new
name|TimeRange
argument_list|()
expr_stmt|;
name|tr
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|numFamilies
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|familyMap
operator|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
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
name|numFamilies
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|numColumns
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|set
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numColumns
condition|;
name|j
operator|++
control|)
block|{
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|set
operator|.
name|add
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|familyMap
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|set
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|startRow
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|stopRow
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|filter
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|HbaseObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|filter
argument_list|,
name|Filter
operator|.
name|class
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|oldFilter
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|HbaseObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|oldFilter
argument_list|,
name|RowFilterInterface
operator|.
name|class
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|tr
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|familyMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|>
name|entry
range|:
name|familyMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columnSet
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|columnSet
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|columnSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|qualifier
range|:
name|columnSet
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

