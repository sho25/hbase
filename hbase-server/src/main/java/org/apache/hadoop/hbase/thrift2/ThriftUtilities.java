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
name|thrift2
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
name|KeyValueUtil
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
name|*
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
name|ParseFilter
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
name|thrift2
operator|.
name|generated
operator|.
name|*
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|util
operator|.
name|Bytes
operator|.
name|getBytes
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ThriftUtilities
block|{
specifier|private
name|ThriftUtilities
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't initialize class"
argument_list|)
throw|;
block|}
comment|/**    * Creates a {@link Get} (HBase) from a {@link TGet} (Thrift).    *    * This ignores any timestamps set on {@link TColumn} objects.    *    * @param in the<code>TGet</code> to convert    *    * @return<code>Get</code> object    *    * @throws IOException if an invalid time range or max version parameter is given    */
specifier|public
specifier|static
name|Get
name|getFromThrift
parameter_list|(
name|TGet
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|out
init|=
operator|new
name|Get
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
comment|// Timestamp overwrites time range if both are set
if|if
condition|(
name|in
operator|.
name|isSetTimestamp
argument_list|()
condition|)
block|{
name|out
operator|.
name|setTimeStamp
argument_list|(
name|in
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|in
operator|.
name|isSetTimeRange
argument_list|()
condition|)
block|{
name|out
operator|.
name|setTimeRange
argument_list|(
name|in
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMinStamp
argument_list|()
argument_list|,
name|in
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMaxStamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetMaxVersions
argument_list|()
condition|)
block|{
name|out
operator|.
name|setMaxVersions
argument_list|(
name|in
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetFilterString
argument_list|()
condition|)
block|{
name|ParseFilter
name|parseFilter
init|=
operator|new
name|ParseFilter
argument_list|()
decl_stmt|;
name|out
operator|.
name|setFilter
argument_list|(
name|parseFilter
operator|.
name|parseFilterString
argument_list|(
name|in
operator|.
name|getFilterString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetAttributes
argument_list|()
condition|)
block|{
name|addAttributes
argument_list|(
name|out
argument_list|,
name|in
operator|.
name|getAttributes
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|in
operator|.
name|isSetColumns
argument_list|()
condition|)
block|{
return|return
name|out
return|;
block|}
for|for
control|(
name|TColumn
name|column
range|:
name|in
operator|.
name|getColumns
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|isSetQualifier
argument_list|()
condition|)
block|{
name|out
operator|.
name|addColumn
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|addFamily
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|out
return|;
block|}
comment|/**    * Converts multiple {@link TGet}s (Thrift) into a list of {@link Get}s (HBase).    *    * @param in list of<code>TGet</code>s to convert    *    * @return list of<code>Get</code> objects    *    * @throws IOException if an invalid time range or max version parameter is given    * @see #getFromThrift(TGet)    */
specifier|public
specifier|static
name|List
argument_list|<
name|Get
argument_list|>
name|getsFromThrift
parameter_list|(
name|List
argument_list|<
name|TGet
argument_list|>
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Get
argument_list|>
name|out
init|=
operator|new
name|ArrayList
argument_list|<
name|Get
argument_list|>
argument_list|(
name|in
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TGet
name|get
range|:
name|in
control|)
block|{
name|out
operator|.
name|add
argument_list|(
name|getFromThrift
argument_list|(
name|get
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|out
return|;
block|}
comment|/**    * Creates a {@link TResult} (Thrift) from a {@link Result} (HBase).    *    * @param in the<code>Result</code> to convert    *    * @return converted result, returns an empty result if the input is<code>null</code>    */
specifier|public
specifier|static
name|TResult
name|resultFromHBase
parameter_list|(
name|Result
name|in
parameter_list|)
block|{
name|KeyValue
index|[]
name|raw
init|=
name|in
operator|.
name|raw
argument_list|()
decl_stmt|;
name|TResult
name|out
init|=
operator|new
name|TResult
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|in
operator|.
name|getRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|setRow
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|TColumnValue
argument_list|>
name|columnValues
init|=
operator|new
name|ArrayList
argument_list|<
name|TColumnValue
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|raw
control|)
block|{
name|TColumnValue
name|col
init|=
operator|new
name|TColumnValue
argument_list|()
decl_stmt|;
name|col
operator|.
name|setFamily
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
expr_stmt|;
name|col
operator|.
name|setQualifier
argument_list|(
name|kv
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
name|col
operator|.
name|setTimestamp
argument_list|(
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|col
operator|.
name|setValue
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|columnValues
operator|.
name|add
argument_list|(
name|col
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|setColumnValues
argument_list|(
name|columnValues
argument_list|)
expr_stmt|;
return|return
name|out
return|;
block|}
comment|/**    * Converts multiple {@link Result}s (HBase) into a list of {@link TResult}s (Thrift).    *    * @param in array of<code>Result</code>s to convert    *    * @return list of converted<code>TResult</code>s    *    * @see #resultFromHBase(Result)    */
specifier|public
specifier|static
name|List
argument_list|<
name|TResult
argument_list|>
name|resultsFromHBase
parameter_list|(
name|Result
index|[]
name|in
parameter_list|)
block|{
name|List
argument_list|<
name|TResult
argument_list|>
name|out
init|=
operator|new
name|ArrayList
argument_list|<
name|TResult
argument_list|>
argument_list|(
name|in
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|in
control|)
block|{
name|out
operator|.
name|add
argument_list|(
name|resultFromHBase
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|out
return|;
block|}
comment|/**    * Creates a {@link Put} (HBase) from a {@link TPut} (Thrift)    *    * @param in the<code>TPut</code> to convert    *    * @return converted<code>Put</code>    */
specifier|public
specifier|static
name|Put
name|putFromThrift
parameter_list|(
name|TPut
name|in
parameter_list|)
block|{
name|Put
name|out
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|isSetTimestamp
argument_list|()
condition|)
block|{
name|out
operator|=
operator|new
name|Put
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|,
name|in
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|=
operator|new
name|Put
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|setDurability
argument_list|(
name|in
operator|.
name|isWriteToWal
argument_list|()
condition|?
name|Durability
operator|.
name|SYNC_WAL
else|:
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
for|for
control|(
name|TColumnValue
name|columnValue
range|:
name|in
operator|.
name|getColumnValues
argument_list|()
control|)
block|{
if|if
condition|(
name|columnValue
operator|.
name|isSetTimestamp
argument_list|()
condition|)
block|{
name|out
operator|.
name|add
argument_list|(
name|columnValue
operator|.
name|getFamily
argument_list|()
argument_list|,
name|columnValue
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|columnValue
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|columnValue
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|add
argument_list|(
name|columnValue
operator|.
name|getFamily
argument_list|()
argument_list|,
name|columnValue
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|columnValue
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|in
operator|.
name|isSetAttributes
argument_list|()
condition|)
block|{
name|addAttributes
argument_list|(
name|out
argument_list|,
name|in
operator|.
name|getAttributes
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|out
return|;
block|}
comment|/**    * Converts multiple {@link TPut}s (Thrift) into a list of {@link Put}s (HBase).    *    * @param in list of<code>TPut</code>s to convert    *    * @return list of converted<code>Put</code>s    *    * @see #putFromThrift(TPut)    */
specifier|public
specifier|static
name|List
argument_list|<
name|Put
argument_list|>
name|putsFromThrift
parameter_list|(
name|List
argument_list|<
name|TPut
argument_list|>
name|in
parameter_list|)
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|out
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|(
name|in
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TPut
name|put
range|:
name|in
control|)
block|{
name|out
operator|.
name|add
argument_list|(
name|putFromThrift
argument_list|(
name|put
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|out
return|;
block|}
comment|/**    * Creates a {@link Delete} (HBase) from a {@link TDelete} (Thrift).    *    * @param in the<code>TDelete</code> to convert    *    * @return converted<code>Delete</code>    */
specifier|public
specifier|static
name|Delete
name|deleteFromThrift
parameter_list|(
name|TDelete
name|in
parameter_list|)
block|{
name|Delete
name|out
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|isSetColumns
argument_list|()
condition|)
block|{
name|out
operator|=
operator|new
name|Delete
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TColumn
name|column
range|:
name|in
operator|.
name|getColumns
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|isSetQualifier
argument_list|()
condition|)
block|{
if|if
condition|(
name|column
operator|.
name|isSetTimestamp
argument_list|()
condition|)
block|{
if|if
condition|(
name|in
operator|.
name|isSetDeleteType
argument_list|()
operator|&&
name|in
operator|.
name|getDeleteType
argument_list|()
operator|.
name|equals
argument_list|(
name|TDeleteType
operator|.
name|DELETE_COLUMNS
argument_list|)
condition|)
name|out
operator|.
name|deleteColumns
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|column
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
else|else
name|out
operator|.
name|deleteColumn
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|column
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|in
operator|.
name|isSetDeleteType
argument_list|()
operator|&&
name|in
operator|.
name|getDeleteType
argument_list|()
operator|.
name|equals
argument_list|(
name|TDeleteType
operator|.
name|DELETE_COLUMNS
argument_list|)
condition|)
name|out
operator|.
name|deleteColumns
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
else|else
name|out
operator|.
name|deleteColumn
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|column
operator|.
name|isSetTimestamp
argument_list|()
condition|)
block|{
name|out
operator|.
name|deleteFamily
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|deleteFamily
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|in
operator|.
name|isSetTimestamp
argument_list|()
condition|)
block|{
name|out
operator|=
operator|new
name|Delete
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|,
name|in
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|=
operator|new
name|Delete
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|in
operator|.
name|isSetAttributes
argument_list|()
condition|)
block|{
name|addAttributes
argument_list|(
name|out
argument_list|,
name|in
operator|.
name|getAttributes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|setDurability
argument_list|(
name|in
operator|.
name|isWriteToWal
argument_list|()
condition|?
name|Durability
operator|.
name|SYNC_WAL
else|:
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
return|return
name|out
return|;
block|}
comment|/**    * Converts multiple {@link TDelete}s (Thrift) into a list of {@link Delete}s (HBase).    *    * @param in list of<code>TDelete</code>s to convert    *    * @return list of converted<code>Delete</code>s    *    * @see #deleteFromThrift(TDelete)    */
specifier|public
specifier|static
name|List
argument_list|<
name|Delete
argument_list|>
name|deletesFromThrift
parameter_list|(
name|List
argument_list|<
name|TDelete
argument_list|>
name|in
parameter_list|)
block|{
name|List
argument_list|<
name|Delete
argument_list|>
name|out
init|=
operator|new
name|ArrayList
argument_list|<
name|Delete
argument_list|>
argument_list|(
name|in
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TDelete
name|delete
range|:
name|in
control|)
block|{
name|out
operator|.
name|add
argument_list|(
name|deleteFromThrift
argument_list|(
name|delete
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|out
return|;
block|}
specifier|public
specifier|static
name|TDelete
name|deleteFromHBase
parameter_list|(
name|Delete
name|in
parameter_list|)
block|{
name|TDelete
name|out
init|=
operator|new
name|TDelete
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TColumn
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|TColumn
argument_list|>
argument_list|()
decl_stmt|;
name|long
name|rowTimestamp
init|=
name|in
operator|.
name|getTimeStamp
argument_list|()
decl_stmt|;
if|if
condition|(
name|rowTimestamp
operator|!=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
condition|)
block|{
name|out
operator|.
name|setTimestamp
argument_list|(
name|rowTimestamp
argument_list|)
expr_stmt|;
block|}
comment|// Map<family, List<KeyValue>>
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Cell
argument_list|>
argument_list|>
name|familyEntry
range|:
name|in
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TColumn
name|column
init|=
operator|new
name|TColumn
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|familyEntry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Cell
name|cell
range|:
name|familyEntry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|kv
operator|.
name|getFamily
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|kv
operator|.
name|getQualifier
argument_list|()
decl_stmt|;
name|long
name|timestamp
init|=
name|kv
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
if|if
condition|(
name|family
operator|!=
literal|null
condition|)
block|{
name|column
operator|.
name|setFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|column
operator|.
name|setQualifier
argument_list|(
name|qualifier
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timestamp
operator|!=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
condition|)
block|{
name|column
operator|.
name|setTimestamp
argument_list|(
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|columns
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|setColumns
argument_list|(
name|columns
argument_list|)
expr_stmt|;
return|return
name|out
return|;
block|}
comment|/**    * Creates a {@link RowMutations} (HBase) from a {@link TRowMutations} (Thrift)    *    * @param in the<code>TRowMutations</code> to convert    *    * @return converted<code>RowMutations</code>    */
specifier|public
specifier|static
name|RowMutations
name|rowMutationsFromThrift
parameter_list|(
name|TRowMutations
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|RowMutations
name|out
init|=
operator|new
name|RowMutations
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TMutation
argument_list|>
name|mutations
init|=
name|in
operator|.
name|getMutations
argument_list|()
decl_stmt|;
for|for
control|(
name|TMutation
name|mutation
range|:
name|mutations
control|)
block|{
if|if
condition|(
name|mutation
operator|.
name|isSetPut
argument_list|()
condition|)
block|{
name|out
operator|.
name|add
argument_list|(
name|putFromThrift
argument_list|(
name|mutation
operator|.
name|getPut
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mutation
operator|.
name|isSetDeleteSingle
argument_list|()
condition|)
block|{
name|out
operator|.
name|add
argument_list|(
name|deleteFromThrift
argument_list|(
name|mutation
operator|.
name|getDeleteSingle
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|out
return|;
block|}
specifier|public
specifier|static
name|Scan
name|scanFromThrift
parameter_list|(
name|TScan
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|out
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|isSetStartRow
argument_list|()
condition|)
name|out
operator|.
name|setStartRow
argument_list|(
name|in
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|isSetStopRow
argument_list|()
condition|)
name|out
operator|.
name|setStopRow
argument_list|(
name|in
operator|.
name|getStopRow
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|isSetCaching
argument_list|()
condition|)
name|out
operator|.
name|setCaching
argument_list|(
name|in
operator|.
name|getCaching
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|isSetMaxVersions
argument_list|()
condition|)
block|{
name|out
operator|.
name|setMaxVersions
argument_list|(
name|in
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetColumns
argument_list|()
condition|)
block|{
for|for
control|(
name|TColumn
name|column
range|:
name|in
operator|.
name|getColumns
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|isSetQualifier
argument_list|()
condition|)
block|{
name|out
operator|.
name|addColumn
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|addFamily
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|TTimeRange
name|timeRange
init|=
name|in
operator|.
name|getTimeRange
argument_list|()
decl_stmt|;
if|if
condition|(
name|timeRange
operator|!=
literal|null
operator|&&
name|timeRange
operator|.
name|isSetMinStamp
argument_list|()
operator|&&
name|timeRange
operator|.
name|isSetMaxStamp
argument_list|()
condition|)
block|{
name|out
operator|.
name|setTimeRange
argument_list|(
name|timeRange
operator|.
name|getMinStamp
argument_list|()
argument_list|,
name|timeRange
operator|.
name|getMaxStamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetBatchSize
argument_list|()
condition|)
block|{
name|out
operator|.
name|setBatch
argument_list|(
name|in
operator|.
name|getBatchSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetFilterString
argument_list|()
condition|)
block|{
name|ParseFilter
name|parseFilter
init|=
operator|new
name|ParseFilter
argument_list|()
decl_stmt|;
name|out
operator|.
name|setFilter
argument_list|(
name|parseFilter
operator|.
name|parseFilterString
argument_list|(
name|in
operator|.
name|getFilterString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetAttributes
argument_list|()
condition|)
block|{
name|addAttributes
argument_list|(
name|out
argument_list|,
name|in
operator|.
name|getAttributes
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|out
return|;
block|}
specifier|public
specifier|static
name|Increment
name|incrementFromThrift
parameter_list|(
name|TIncrement
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Increment
name|out
init|=
operator|new
name|Increment
argument_list|(
name|in
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TColumnIncrement
name|column
range|:
name|in
operator|.
name|getColumns
argument_list|()
control|)
block|{
name|out
operator|.
name|addColumn
argument_list|(
name|column
operator|.
name|getFamily
argument_list|()
argument_list|,
name|column
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|column
operator|.
name|getAmount
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|isSetAttributes
argument_list|()
condition|)
block|{
name|addAttributes
argument_list|(
name|out
argument_list|,
name|in
operator|.
name|getAttributes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|setDurability
argument_list|(
name|in
operator|.
name|isWriteToWal
argument_list|()
condition|?
name|Durability
operator|.
name|SYNC_WAL
else|:
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
return|return
name|out
return|;
block|}
comment|/**    * Adds all the attributes into the Operation object    */
specifier|private
specifier|static
name|void
name|addAttributes
parameter_list|(
name|OperationWithAttributes
name|op
parameter_list|,
name|Map
argument_list|<
name|ByteBuffer
argument_list|,
name|ByteBuffer
argument_list|>
name|attributes
parameter_list|)
block|{
if|if
condition|(
name|attributes
operator|==
literal|null
operator|||
name|attributes
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ByteBuffer
argument_list|,
name|ByteBuffer
argument_list|>
name|entry
range|:
name|attributes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getBytes
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|getBytes
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|op
operator|.
name|setAttribute
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

