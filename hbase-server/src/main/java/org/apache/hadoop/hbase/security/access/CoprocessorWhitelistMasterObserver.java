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
name|security
operator|.
name|access
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|PathMatcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FilenameUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|fs
operator|.
name|Path
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
name|coprocessor
operator|.
name|BaseMasterObserver
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
name|MasterCoprocessorEnvironment
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
name|ObserverContext
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
name|HBaseInterfaceAudience
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|master
operator|.
name|MasterServices
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
name|TableName
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
comment|/**  * Master observer for restricting coprocessor assignments.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|CoprocessorWhitelistMasterObserver
extends|extends
name|BaseMasterObserver
block|{
specifier|public
specifier|static
specifier|final
name|String
name|CP_COPROCESSOR_WHITELIST_PATHS_KEY
init|=
literal|"hbase.coprocessor.region.whitelist.paths"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CoprocessorWhitelistMasterObserver
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preModifyTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyCoprocessors
argument_list|(
name|ctx
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCreateTable
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyCoprocessors
argument_list|(
name|ctx
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
comment|/**    * Validates a single whitelist path against the coprocessor path    * @param  coprocPath the path to the coprocessor including scheme    * @param  wlPath     can be:    *                      1) a "*" to wildcard all coprocessor paths    *                      2) a specific filesystem (e.g. hdfs://my-cluster/)    *                      3) a wildcard path to be evaluated by    *                         {@link FilenameUtils.wildcardMatch}    *                         path can specify scheme or not (e.g.    *                         "file:///usr/hbase/coprocessors" or for all    *                         filesystems "/usr/hbase/coprocessors")    * @return             if the path was found under the wlPath    * @throws IOException if a failure occurs in getting the path file system    */
specifier|private
specifier|static
name|boolean
name|validatePath
parameter_list|(
name|Path
name|coprocPath
parameter_list|,
name|Path
name|wlPath
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// verify if all are allowed
if|if
condition|(
name|wlPath
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
return|return
operator|(
literal|true
operator|)
return|;
block|}
comment|// verify we are on the same filesystem if wlPath has a scheme
if|if
condition|(
operator|!
name|wlPath
operator|.
name|isAbsoluteAndSchemeAuthorityNull
argument_list|()
condition|)
block|{
name|String
name|wlPathScheme
init|=
name|wlPath
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|coprocPathScheme
init|=
name|coprocPath
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|wlPathHost
init|=
name|wlPath
operator|.
name|toUri
argument_list|()
operator|.
name|getHost
argument_list|()
decl_stmt|;
name|String
name|coprocPathHost
init|=
name|coprocPath
operator|.
name|toUri
argument_list|()
operator|.
name|getHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|wlPathScheme
operator|!=
literal|null
condition|)
block|{
name|wlPathScheme
operator|=
name|wlPathScheme
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|wlPathScheme
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
name|wlPathHost
operator|!=
literal|null
condition|)
block|{
name|wlPathHost
operator|=
name|wlPathHost
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|wlPathHost
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
name|coprocPathScheme
operator|!=
literal|null
condition|)
block|{
name|coprocPathScheme
operator|=
name|coprocPathScheme
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|coprocPathScheme
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
name|coprocPathHost
operator|!=
literal|null
condition|)
block|{
name|coprocPathHost
operator|=
name|coprocPathHost
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|coprocPathHost
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|wlPathScheme
operator|.
name|equals
argument_list|(
name|coprocPathScheme
argument_list|)
operator|||
operator|!
name|wlPathHost
operator|.
name|equals
argument_list|(
name|coprocPathHost
argument_list|)
condition|)
block|{
return|return
operator|(
literal|false
operator|)
return|;
block|}
block|}
comment|// allow any on this file-system (file systems were verified to be the same above)
if|if
condition|(
name|wlPath
operator|.
name|isRoot
argument_list|()
condition|)
block|{
return|return
operator|(
literal|true
operator|)
return|;
block|}
comment|// allow "loose" matches stripping scheme
if|if
condition|(
name|FilenameUtils
operator|.
name|wildcardMatch
argument_list|(
name|Path
operator|.
name|getPathWithoutSchemeAndAuthority
argument_list|(
name|coprocPath
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|Path
operator|.
name|getPathWithoutSchemeAndAuthority
argument_list|(
name|wlPath
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
operator|(
literal|true
operator|)
return|;
block|}
return|return
operator|(
literal|false
operator|)
return|;
block|}
comment|/**    * Perform the validation checks for a coprocessor to determine if the path    * is white listed or not.    * @throws IOException if path is not included in whitelist or a failure    *                     occurs in processing    * @param  ctx         as passed in from the coprocessor    * @param  htd         as passed in from the coprocessor    */
specifier|private
name|void
name|verifyCoprocessors
parameter_list|(
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|MasterServices
name|services
init|=
name|ctx
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getMasterServices
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|services
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|paths
init|=
name|conf
operator|.
name|getStringCollection
argument_list|(
name|CP_COPROCESSOR_WHITELIST_PATHS_KEY
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|coprocs
init|=
name|htd
operator|.
name|getCoprocessors
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|coprocs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|coproc
init|=
name|coprocs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|coprocSpec
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|htd
operator|.
name|getValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"coprocessor$"
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|coprocSpec
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// File path is the 1st field of the coprocessor spec
name|Matcher
name|matcher
init|=
name|HConstants
operator|.
name|CP_HTD_ATTR_VALUE_PATTERN
operator|.
name|matcher
argument_list|(
name|coprocSpec
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|==
literal|null
operator|||
operator|!
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|String
name|coprocPathStr
init|=
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
comment|// Check if coprocessor is being loaded via the classpath (i.e. no file path)
if|if
condition|(
name|coprocPathStr
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
break|break;
block|}
name|Path
name|coprocPath
init|=
operator|new
name|Path
argument_list|(
name|coprocPathStr
argument_list|)
decl_stmt|;
name|String
name|coprocessorClass
init|=
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|boolean
name|foundPathMatch
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|pathStr
range|:
name|paths
control|)
block|{
name|Path
name|wlPath
init|=
operator|new
name|Path
argument_list|(
name|pathStr
argument_list|)
decl_stmt|;
try|try
block|{
name|foundPathMatch
operator|=
name|validatePath
argument_list|(
name|coprocPath
argument_list|,
name|wlPath
argument_list|,
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|foundPathMatch
operator|==
literal|true
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Coprocessor %s found in directory %s"
argument_list|,
name|coprocessorClass
argument_list|,
name|pathStr
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Failed to validate white list path %s for coprocessor path %s"
argument_list|,
name|pathStr
argument_list|,
name|coprocPathStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|foundPathMatch
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Loading %s DENIED in %s"
argument_list|,
name|coprocessorClass
argument_list|,
name|CP_COPROCESSOR_WHITELIST_PATHS_KEY
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

