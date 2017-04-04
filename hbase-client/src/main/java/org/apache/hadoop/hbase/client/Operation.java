begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|JsonMapper
import|;
end_import

begin_comment
comment|/**  * Superclass for any type that maps to a potentially application-level query.  * (e.g. Put, Get, Delete, Scan, Next, etc.)  * Contains methods for exposure to logging and debugging tools.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
specifier|abstract
class|class
name|Operation
block|{
comment|// TODO make this configurable
comment|// TODO Do we need this anymore now we have protobuffed it all?
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_COLS
init|=
literal|5
decl_stmt|;
comment|/**    * Produces a Map containing a fingerprint which identifies the type and     * the static schema components of a query (i.e. column families)    * @return a map containing fingerprint information (i.e. column families)    */
specifier|public
specifier|abstract
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getFingerprint
parameter_list|()
function_decl|;
comment|/**    * Produces a Map containing a summary of the details of a query     * beyond the scope of the fingerprint (i.e. columns, rows...)    * @param maxCols a limit on the number of columns output prior to truncation    * @return a map containing parameters of a query (i.e. rows, columns...)    */
specifier|public
specifier|abstract
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|(
name|int
name|maxCols
parameter_list|)
function_decl|;
comment|/**    * Produces a Map containing a full summary of a query.    * @return a map containing parameters of a query (i.e. rows, columns...)    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|()
block|{
return|return
name|toMap
argument_list|(
name|DEFAULT_MAX_COLS
argument_list|)
return|;
block|}
comment|/**    * Produces a JSON object for fingerprint and details exposure in a    * parseable format.    * @param maxCols a limit on the number of columns to include in the JSON    * @return a JSONObject containing this Operation's information, as a string    */
specifier|public
name|String
name|toJSON
parameter_list|(
name|int
name|maxCols
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|JsonMapper
operator|.
name|writeMapAsString
argument_list|(
name|toMap
argument_list|(
name|maxCols
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Produces a JSON object sufficient for description of a query    * in a debugging or logging context.    * @return the produced JSON object, as a string    */
specifier|public
name|String
name|toJSON
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|toJSON
argument_list|(
name|DEFAULT_MAX_COLS
argument_list|)
return|;
block|}
comment|/**    * Produces a string representation of this Operation. It defaults to a JSON    * representation, but falls back to a string representation of the     * fingerprint and details in the case of a JSON encoding failure.    * @param maxCols a limit on the number of columns output in the summary    * prior to truncation    * @return a JSON-parseable String    */
specifier|public
name|String
name|toString
parameter_list|(
name|int
name|maxCols
parameter_list|)
block|{
comment|/* for now this is merely a wrapper from producing a JSON string, but       * toJSON is kept separate in case this is changed to be a less parsable      * pretty printed representation.      */
try|try
block|{
return|return
name|toJSON
argument_list|(
name|maxCols
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
return|return
name|toMap
argument_list|(
name|maxCols
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|/**    * Produces a string representation of this Operation. It defaults to a JSON    * representation, but falls back to a string representation of the    * fingerprint and details in the case of a JSON encoding failure.    * @return String    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|toString
argument_list|(
name|DEFAULT_MAX_COLS
argument_list|)
return|;
block|}
block|}
end_class

end_unit

