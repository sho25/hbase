begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Used by {@link HBaseFsck} reporting system.  * @deprecated Since 2.3.0. To be removed in hbase4. Use HBCK2 instead. Remove when  *   {@link HBaseFsck} is removed.  */
end_comment

begin_interface
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|HbckErrorReporter
block|{
enum|enum
name|ERROR_CODE
block|{
name|UNKNOWN
block|,
name|NO_META_REGION
block|,
name|NULL_META_REGION
block|,
name|NO_VERSION_FILE
block|,
name|NOT_IN_META_HDFS
block|,
name|NOT_IN_META
block|,
name|NOT_IN_META_OR_DEPLOYED
block|,
name|NOT_IN_HDFS_OR_DEPLOYED
block|,
name|NOT_IN_HDFS
block|,
name|SERVER_DOES_NOT_MATCH_META
block|,
name|NOT_DEPLOYED
block|,
name|MULTI_DEPLOYED
block|,
name|SHOULD_NOT_BE_DEPLOYED
block|,
name|MULTI_META_REGION
block|,
name|RS_CONNECT_FAILURE
block|,
name|FIRST_REGION_STARTKEY_NOT_EMPTY
block|,
name|LAST_REGION_ENDKEY_NOT_EMPTY
block|,
name|DUPE_STARTKEYS
block|,
name|HOLE_IN_REGION_CHAIN
block|,
name|OVERLAP_IN_REGION_CHAIN
block|,
name|REGION_CYCLE
block|,
name|DEGENERATE_REGION
block|,
name|ORPHAN_HDFS_REGION
block|,
name|LINGERING_SPLIT_PARENT
block|,
name|NO_TABLEINFO_FILE
block|,
name|LINGERING_REFERENCE_HFILE
block|,
name|LINGERING_HFILELINK
block|,
name|WRONG_USAGE
block|,
name|EMPTY_META_CELL
block|,
name|EXPIRED_TABLE_LOCK
block|,
name|BOUNDARIES_ERROR
block|,
name|ORPHAN_TABLE_STATE
block|,
name|NO_TABLE_STATE
block|,
name|UNDELETED_REPLICATION_QUEUE
block|,
name|DUPE_ENDKEYS
block|,
name|UNSUPPORTED_OPTION
block|,
name|INVALID_TABLE
block|}
name|void
name|clear
parameter_list|()
function_decl|;
name|void
name|report
parameter_list|(
name|String
name|message
parameter_list|)
function_decl|;
name|void
name|reportError
parameter_list|(
name|String
name|message
parameter_list|)
function_decl|;
name|void
name|reportError
parameter_list|(
name|ERROR_CODE
name|errorCode
parameter_list|,
name|String
name|message
parameter_list|)
function_decl|;
name|void
name|reportError
parameter_list|(
name|ERROR_CODE
name|errorCode
parameter_list|,
name|String
name|message
parameter_list|,
name|HbckTableInfo
name|table
parameter_list|)
function_decl|;
name|void
name|reportError
parameter_list|(
name|ERROR_CODE
name|errorCode
parameter_list|,
name|String
name|message
parameter_list|,
name|HbckTableInfo
name|table
parameter_list|,
name|HbckRegionInfo
name|info
parameter_list|)
function_decl|;
name|void
name|reportError
parameter_list|(
name|ERROR_CODE
name|errorCode
parameter_list|,
name|String
name|message
parameter_list|,
name|HbckTableInfo
name|table
parameter_list|,
name|HbckRegionInfo
name|info1
parameter_list|,
name|HbckRegionInfo
name|info2
parameter_list|)
function_decl|;
name|int
name|summarize
parameter_list|()
function_decl|;
name|void
name|detail
parameter_list|(
name|String
name|details
parameter_list|)
function_decl|;
name|ArrayList
argument_list|<
name|ERROR_CODE
argument_list|>
name|getErrorList
parameter_list|()
function_decl|;
name|void
name|progress
parameter_list|()
function_decl|;
name|void
name|print
parameter_list|(
name|String
name|message
parameter_list|)
function_decl|;
name|void
name|resetErrors
parameter_list|()
function_decl|;
name|boolean
name|tableHasErrors
parameter_list|(
name|HbckTableInfo
name|table
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

