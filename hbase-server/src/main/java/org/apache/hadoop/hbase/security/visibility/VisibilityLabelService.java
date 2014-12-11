begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|visibility
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
name|List
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
name|Configurable
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
name|Tag
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
name|TagType
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
name|hbase
operator|.
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|OperationStatus
import|;
end_import

begin_comment
comment|/**  * The interface which deals with visibility labels and user auths admin service as well as the cell  * visibility expression storage part and read time evaluation.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|VisibilityLabelService
extends|extends
name|Configurable
block|{
comment|/**    * System calls this after opening of regions. Gives a chance for the VisibilityLabelService to so    * any initialization logic.    * @param e    *          the region coprocessor env    */
name|void
name|init
parameter_list|(
name|RegionCoprocessorEnvironment
name|e
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Adds the set of labels into the system.    * @param labels    *          Labels to add to the system.    * @return OperationStatus for each of the label addition    */
name|OperationStatus
index|[]
name|addLabels
parameter_list|(
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|labels
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Sets given labels globally authorized for the user.    * @param user    *          The authorizing user    * @param authLabels    *          Labels which are getting authorized for the user    * @return OperationStatus for each of the label auth addition    */
name|OperationStatus
index|[]
name|setAuths
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|authLabels
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Removes given labels from user's globally authorized list of labels.    * @param user    *          The user whose authorization to be removed    * @param authLabels    *          Labels which are getting removed from authorization set    * @return OperationStatus for each of the label auth removal    */
name|OperationStatus
index|[]
name|clearAuths
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|authLabels
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param user    *          Name of the user whose authorization to be retrieved    * @param systemCall    *          Whether a system or user originated call.    * @return Visibility labels authorized for the given user.    */
name|List
argument_list|<
name|String
argument_list|>
name|getAuths
parameter_list|(
name|byte
index|[]
name|user
parameter_list|,
name|boolean
name|systemCall
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Retrieve the list of visibility labels defined in the system.    * @param regex  The regular expression to filter which labels are returned.    * @return List of visibility labels    */
name|List
argument_list|<
name|String
argument_list|>
name|listLabels
parameter_list|(
name|String
name|regex
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Creates tags corresponding to given visibility expression.    *<br>    * Note: This will be concurrently called from multiple threads and implementation should    * take care of thread safety.    * @param visExpression The Expression for which corresponding Tags to be created.    * @param withSerializationFormat specifies whether a tag, denoting the serialization version    *          of the tags, to be added in the list. When this is true make sure to add the    *          serialization format Tag also. The format tag value should be byte type.    * @param checkAuths denotes whether to check individual labels in visExpression against user's    *          global auth label.    * @return The list of tags corresponds to the visibility expression. These tags will be stored    *         along with the Cells.    */
name|List
argument_list|<
name|Tag
argument_list|>
name|createVisibilityExpTags
parameter_list|(
name|String
name|visExpression
parameter_list|,
name|boolean
name|withSerializationFormat
parameter_list|,
name|boolean
name|checkAuths
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Creates VisibilityExpEvaluator corresponding to given Authorizations.<br>    * Note: This will be concurrently called from multiple threads and implementation should take    * care of thread safety.    * @param authorizations    *          Authorizations for the read request    * @return The VisibilityExpEvaluator corresponding to the given set of authorization labels.    */
name|VisibilityExpEvaluator
name|getVisibilityExpEvaluator
parameter_list|(
name|Authorizations
name|authorizations
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * System checks for user auth during admin operations. (ie. Label add, set/clear auth). The    * operation is allowed only for users having system auth. Also during read, if the requesting    * user has system auth, he can view all the data irrespective of its labels.    * @param user    *          User for whom system auth check to be done.    * @return true if the given user is having system/super auth    */
name|boolean
name|havingSystemAuth
parameter_list|(
name|byte
index|[]
name|user
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * System uses this for deciding whether a Cell can be deleted by matching visibility expression    * in Delete mutation and the cell in consideration. Also system passes the serialization format    * of visibility tags in Put and Delete.<br>    * Note: This will be concurrently called from multiple threads and implementation should take    * care of thread safety.    * @param putVisTags    *          The visibility tags present in the Put mutation    * @param putVisTagFormat    *          The serialization format for the Put visibility tags. A<code>null</code> value for    *          this format means the tags are written with unsorted label ordinals    * @param deleteVisTags    *          - The visibility tags in the delete mutation (the specified Cell Visibility)    * @param deleteVisTagFormat    *          The serialization format for the Delete visibility tags. A<code>null</code> value for    *          this format means the tags are written with unsorted label ordinals    * @return true if matching tags are found    * @see VisibilityConstants#SORTED_ORDINAL_SERIALIZATION_FORMAT    */
name|boolean
name|matchVisibility
parameter_list|(
name|List
argument_list|<
name|Tag
argument_list|>
name|putVisTags
parameter_list|,
name|Byte
name|putVisTagFormat
parameter_list|,
name|List
argument_list|<
name|Tag
argument_list|>
name|deleteVisTags
parameter_list|,
name|Byte
name|deleteVisTagFormat
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Provides a way to modify the visibility tags of type {@link TagType}    * .VISIBILITY_TAG_TYPE, that are part of the cell created from the WALEdits    * that are prepared for replication while calling    * {@link org.apache.hadoop.hbase.replication.ReplicationEndpoint}    * .replicate().    * {@link org.apache.hadoop.hbase.security.visibility.VisibilityReplicationEndpoint}    * calls this API to provide an opportunity to modify the visibility tags    * before replicating.    *    * @param visTags    *          the visibility tags associated with the cell    * @param serializationFormat    *          the serialization format associated with the tag    * @return the modified visibility expression in the form of byte[]    * @throws IOException    */
name|byte
index|[]
name|encodeVisibilityForReplication
parameter_list|(
specifier|final
name|List
argument_list|<
name|Tag
argument_list|>
name|visTags
parameter_list|,
specifier|final
name|Byte
name|serializationFormat
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

