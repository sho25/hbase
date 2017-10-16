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
name|coprocessor
package|;
end_package

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

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|ElementType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Inherited
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Target
import|;
end_import

begin_comment
comment|/**  * Marker annotation that denotes Coprocessors that are core to HBase.  * A Core Coprocessor is a CP that realizes a core HBase feature. Features are sometimes  * implemented first as a Coprocessor to prove viability. The idea is that once proven, they then  * migrate to core. Meantime, HBase Core Coprocessors get this annotation. No other Coprocessors  * can carry this annotation.  */
end_comment

begin_comment
comment|// Core Coprocessors are generally naughty making use of HBase internals doing accesses no
end_comment

begin_comment
comment|// Coprocessor should be up to so we mark these special Coprocessors with this annotation and on
end_comment

begin_comment
comment|// Coprocessor load, we'll give these Coprocessors a 'richer' Environment with access to internals
end_comment

begin_comment
comment|// not allowed other Coprocessors. see the *CoprocessorHost where they do the Coprocessor loadings.
end_comment

begin_annotation_defn
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|TYPE
argument_list|)
annotation|@
name|Inherited
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
comment|// This Annotation is not @Documented because I don't want users figuring out its mechanics.
specifier|public
annotation_defn|@interface
name|CoreCoprocessor
block|{}
end_annotation_defn

end_unit

