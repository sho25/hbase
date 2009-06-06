begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|QueryMatcher
operator|.
name|MatchCode
import|;
end_import

begin_comment
comment|/**  * Implementing classes of this interface will be used for the tracking  * and enforcement of columns and numbers of versions during the course of a   * Get or Scan operation.  *<p>  * Currently there are two different types of Store/Family-level queries.  *<ul><li>{@link ExplicitColumnTracker} is used when the query specifies  * one or more column qualifiers to return in the family.  *<li>{@link WildcardColumnTracker} is used when the query asks for all  * qualifiers within the family.  *<p>  * This class is utilized by {@link QueryMatcher} through two methods:  *<ul><li>{@link checkColumn} is called when a Put satisfies all other  * conditions of the query.  This method returns a {@link MatchCode} to define  * what action should be taken.  *<li>{@link update} is called at the end of every StoreFile or Memcache.  *<p>  * This class is NOT thread-safe as queries are never multi-threaded   */
end_comment

begin_interface
specifier|public
interface|interface
name|ColumnTracker
block|{
comment|/**    * Keeps track of the number of versions for the columns asked for    * @param bytes    * @param offset    * @param length    * @return    */
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
parameter_list|)
function_decl|;
comment|/**    * Updates internal variables in between files    */
specifier|public
name|void
name|update
parameter_list|()
function_decl|;
comment|/**    * Resets the Matcher    */
specifier|public
name|void
name|reset
parameter_list|()
function_decl|;
comment|/**    *     * @return    */
specifier|public
name|boolean
name|done
parameter_list|()
function_decl|;
comment|/**    * Used by matcher and scan/get to get a hint of the next column    * to seek to after checkColumn() returns SKIP.  Returns the next interesting    * column we want, or NULL there is none (wildcard scanner).    *    * Implementations aren't required to return anything useful unless the most recent    * call was to checkColumn() and the return code was SKIP.  This is pretty implementation    * detail-y, but optimizations are like that.    *    * @return null, or a ColumnCount that we should seek to    */
specifier|public
name|ColumnCount
name|getColumnHint
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

