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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|TagType
block|{
comment|// Please declare new Tag Types here to avoid step on pre-existing tag types.
specifier|public
specifier|static
specifier|final
name|byte
name|ACL_TAG_TYPE
init|=
operator|(
name|byte
operator|)
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|VISIBILITY_TAG_TYPE
init|=
operator|(
name|byte
operator|)
literal|2
decl_stmt|;
comment|// public static final byte LOG_REPLAY_TAG_TYPE = (byte) 3; // deprecated
specifier|public
specifier|static
specifier|final
name|byte
name|VISIBILITY_EXP_SERIALIZATION_FORMAT_TAG_TYPE
init|=
operator|(
name|byte
operator|)
literal|4
decl_stmt|;
comment|// String based tag type used in replication
specifier|public
specifier|static
specifier|final
name|byte
name|STRING_VIS_TAG_TYPE
init|=
operator|(
name|byte
operator|)
literal|7
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
name|TTL_TAG_TYPE
init|=
operator|(
name|byte
operator|)
literal|8
decl_stmt|;
block|}
end_class

end_unit

