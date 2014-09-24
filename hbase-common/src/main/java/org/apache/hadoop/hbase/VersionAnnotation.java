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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * A package attribute that captures the version of hbase that was compiled.  * Copied down from hadoop.  All is same except name of interface.  */
end_comment

begin_annotation_defn
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|PACKAGE
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
annotation_defn|@interface
name|VersionAnnotation
block|{
comment|/**    * Get the Hadoop version    * @return the version string "0.6.3-dev"    */
name|String
name|version
parameter_list|()
function_decl|;
comment|/**    * Get the username that compiled Hadoop.    */
name|String
name|user
parameter_list|()
function_decl|;
comment|/**    * Get the date when Hadoop was compiled.    * @return the date in unix 'date' format    */
name|String
name|date
parameter_list|()
function_decl|;
comment|/**    * Get the url for the subversion repository.    */
name|String
name|url
parameter_list|()
function_decl|;
comment|/**    * Get the subversion revision.    * @return the revision number as a string (eg. "451451")    */
name|String
name|revision
parameter_list|()
function_decl|;
comment|/**    * Get a checksum of the source files from which HBase was compiled.    * @return a string that uniquely identifies the source    **/
name|String
name|srcChecksum
parameter_list|()
function_decl|;
block|}
end_annotation_defn

end_unit

