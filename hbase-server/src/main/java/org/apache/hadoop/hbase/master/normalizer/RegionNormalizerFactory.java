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
name|master
operator|.
name|normalizer
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
name|yetus
operator|.
name|audience
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Factory to create instance of {@link RegionNormalizer} as configured.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RegionNormalizerFactory
block|{
specifier|private
name|RegionNormalizerFactory
parameter_list|()
block|{   }
comment|/**    * Create a region normalizer from the given conf.    * @param conf configuration    * @return {@link RegionNormalizer} implementation    */
specifier|public
specifier|static
name|RegionNormalizer
name|getRegionNormalizer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// Create instance of Region Normalizer
name|Class
argument_list|<
name|?
extends|extends
name|RegionNormalizer
argument_list|>
name|balancerKlass
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|HBASE_MASTER_NORMALIZER_CLASS
argument_list|,
name|SimpleRegionNormalizer
operator|.
name|class
argument_list|,
name|RegionNormalizer
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|balancerKlass
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

