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
name|regionserver
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
name|regionserver
operator|.
name|ScanQueryMatcher
operator|.
name|MatchCode
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

begin_comment
comment|/**  * Keeps track of the columns for a scan if they are not explicitly specified  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ScanWildcardColumnTracker
implements|implements
name|ColumnTracker
block|{
specifier|private
name|byte
index|[]
name|columnBuffer
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|columnOffset
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|columnLength
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|currentCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|maxVersions
decl_stmt|;
specifier|private
name|int
name|minVersions
decl_stmt|;
comment|/* Keeps track of the latest timestamp and type included for current column.    * Used to eliminate duplicates. */
specifier|private
name|long
name|latestTSOfCurrentColumn
decl_stmt|;
specifier|private
name|byte
name|latestTypeOfCurrentColumn
decl_stmt|;
specifier|private
name|long
name|oldestStamp
decl_stmt|;
comment|/**    * Return maxVersions of every row.    * @param minVersion Minimum number of versions to keep    * @param maxVersion Maximum number of versions to return    * @param oldestUnexpiredTS oldest timestamp that has not expired according    *          to the TTL.    */
specifier|public
name|ScanWildcardColumnTracker
parameter_list|(
name|int
name|minVersion
parameter_list|,
name|int
name|maxVersion
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
name|this
operator|.
name|maxVersions
operator|=
name|maxVersion
expr_stmt|;
name|this
operator|.
name|minVersions
operator|=
name|minVersion
expr_stmt|;
name|this
operator|.
name|oldestStamp
operator|=
name|oldestUnexpiredTS
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    * This receives puts *and* deletes.    */
annotation|@
name|Override
specifier|public
name|MatchCode
name|checkColumn
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|byte
name|type
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
comment|/**    * {@inheritDoc}    * This receives puts *and* deletes. Deletes do not count as a version, but rather    * take the version of the previous put (so eventually all but the last can be reclaimed).    */
annotation|@
name|Override
specifier|public
name|ScanQueryMatcher
operator|.
name|MatchCode
name|checkVersions
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
name|type
parameter_list|,
name|boolean
name|ignoreCount
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|columnBuffer
operator|==
literal|null
condition|)
block|{
comment|// first iteration.
name|resetBuffer
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|ignoreCount
condition|)
return|return
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
return|;
comment|// do not count a delete marker as another version
return|return
name|checkVersion
argument_list|(
name|type
argument_list|,
name|timestamp
argument_list|)
return|;
block|}
name|int
name|cmp
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|columnBuffer
argument_list|,
name|columnOffset
argument_list|,
name|columnLength
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|ignoreCount
condition|)
return|return
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
return|;
comment|//If column matches, check if it is a duplicate timestamp
if|if
condition|(
name|sameAsPreviousTSAndType
argument_list|(
name|timestamp
argument_list|,
name|type
argument_list|)
condition|)
block|{
return|return
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SKIP
return|;
block|}
return|return
name|checkVersion
argument_list|(
name|type
argument_list|,
name|timestamp
argument_list|)
return|;
block|}
name|resetTSAndType
argument_list|()
expr_stmt|;
comment|// new col> old col
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
comment|// switched columns, lets do something.x
name|resetBuffer
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|ignoreCount
condition|)
return|return
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
return|;
return|return
name|checkVersion
argument_list|(
name|type
argument_list|,
name|timestamp
argument_list|)
return|;
block|}
comment|// new col< oldcol
comment|// WARNING: This means that very likely an edit for some other family
comment|// was incorrectly stored into the store for this one. Throw an exception,
comment|// because this might lead to data corruption.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"ScanWildcardColumnTracker.checkColumn ran into a column actually "
operator|+
literal|"smaller than the previous column: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
specifier|private
name|void
name|resetBuffer
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|columnBuffer
operator|=
name|bytes
expr_stmt|;
name|columnOffset
operator|=
name|offset
expr_stmt|;
name|columnLength
operator|=
name|length
expr_stmt|;
name|currentCount
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Check whether this version should be retained.    * There are 4 variables considered:    * If this version is past max versions -> skip it    * If this kv has expired or was deleted, check min versions    * to decide whther to skip it or not.    *    * Increase the version counter unless this is a delete    */
specifier|private
name|MatchCode
name|checkVersion
parameter_list|(
name|byte
name|type
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
if|if
condition|(
operator|!
name|KeyValue
operator|.
name|isDelete
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|currentCount
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|currentCount
operator|>
name|maxVersions
condition|)
block|{
return|return
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
comment|// skip to next col
block|}
comment|// keep the KV if required by minversions or it is not expired, yet
if|if
condition|(
name|currentCount
operator|<=
name|minVersions
operator|||
operator|!
name|isExpired
argument_list|(
name|timestamp
argument_list|)
condition|)
block|{
name|setTSAndType
argument_list|(
name|timestamp
argument_list|,
name|type
argument_list|)
expr_stmt|;
return|return
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
else|else
block|{
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|columnBuffer
operator|=
literal|null
expr_stmt|;
name|resetTSAndType
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|resetTSAndType
parameter_list|()
block|{
name|latestTSOfCurrentColumn
operator|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
expr_stmt|;
name|latestTypeOfCurrentColumn
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|void
name|setTSAndType
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|byte
name|type
parameter_list|)
block|{
name|latestTSOfCurrentColumn
operator|=
name|timestamp
expr_stmt|;
name|latestTypeOfCurrentColumn
operator|=
name|type
expr_stmt|;
block|}
specifier|private
name|boolean
name|sameAsPreviousTSAndType
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|byte
name|type
parameter_list|)
block|{
return|return
name|timestamp
operator|==
name|latestTSOfCurrentColumn
operator|&&
name|type
operator|==
name|latestTypeOfCurrentColumn
return|;
block|}
specifier|private
name|boolean
name|isExpired
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
return|return
name|timestamp
operator|<
name|oldestStamp
return|;
block|}
comment|/**    * Used by matcher and scan/get to get a hint of the next column    * to seek to after checkColumn() returns SKIP.  Returns the next interesting    * column we want, or NULL there is none (wildcard scanner).    *    * @return The column count.    */
specifier|public
name|ColumnCount
name|getColumnHint
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * We can never know a-priori if we are done, so always return false.    * @return false    */
annotation|@
name|Override
specifier|public
name|boolean
name|done
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|MatchCode
name|getNextRowOrNextColumn
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|qualLength
parameter_list|)
block|{
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
block|}
specifier|public
name|boolean
name|isDone
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
return|return
name|minVersions
operator|<=
literal|0
operator|&&
name|isExpired
argument_list|(
name|timestamp
argument_list|)
return|;
block|}
block|}
end_class

end_unit

