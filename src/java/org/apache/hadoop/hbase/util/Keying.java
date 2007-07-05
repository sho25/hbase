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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringTokenizer
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * Utility creating hbase friendly keys.  * Use fabricating row names or column qualifiers.  *<p>TODO: Add createSchemeless key, a key that doesn't care if scheme is  * http or https.  */
end_comment

begin_class
specifier|public
class|class
name|Keying
block|{
specifier|private
specifier|static
specifier|final
name|String
name|SCHEME
init|=
literal|"r:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|URI_RE_PARSER
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^([^:/?#]+://(?:[^/?#@]+@)?)([^:/?#]+)(.*)$"
argument_list|)
decl_stmt|;
comment|/**    * Makes a key out of passed URI for use as row name or column qualifier.    *     * This method runs transforms on the passed URI so it sits better    * as a key (or portion-of-a-key) in hbase.  The<code>host</code> portion of    * the URI authority is reversed so subdomains sort under their parent    * domain.  The returned String is an opaque URI of an artificial    *<code>r:</code> scheme to prevent the result being considered an URI of    * the original scheme.  Here is an example of the transform: The url    *<code>http://lucene.apache.org/index.html?query=something#middle<code> is    * returned as    *<code>r:http://org.apache.lucene/index.html?query=something#middle</code>    * The transforms are reversible.  No transform is done if passed URI is    * not hierarchical.    *     *<p>If authority<code>userinfo</code> is present, will mess up the sort    * (until we do more work).</p>    *     * @param u URL to transform.    * @return An opaque URI of artificial 'r' scheme with host portion of URI    * authority reversed (if present).    * @see #keyToUri(String)    * @see<a href="http://www.ietf.org/rfc/rfc2396.txt">RFC2396</a>    */
specifier|public
specifier|static
name|String
name|createKey
parameter_list|(
specifier|final
name|String
name|u
parameter_list|)
block|{
if|if
condition|(
name|u
operator|.
name|startsWith
argument_list|(
name|SCHEME
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Starts with "
operator|+
name|SCHEME
argument_list|)
throw|;
block|}
name|Matcher
name|m
init|=
name|getMatcher
argument_list|(
name|u
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
operator|||
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
comment|// If no match, return original String.
return|return
name|u
return|;
block|}
return|return
name|SCHEME
operator|+
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|+
name|reverseHostname
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|+
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
return|;
block|}
comment|/**    * Reverse the {@link #createKey(String)} transform.    *     * @param s<code>URI</code> made by {@link #createKey(String)}.    * @return 'Restored' URI made by reversing the {@link #createKey(String)}    * transform.    */
specifier|public
specifier|static
name|String
name|keyToUri
parameter_list|(
specifier|final
name|String
name|s
parameter_list|)
block|{
if|if
condition|(
operator|!
name|s
operator|.
name|startsWith
argument_list|(
name|SCHEME
argument_list|)
condition|)
block|{
return|return
name|s
return|;
block|}
name|Matcher
name|m
init|=
name|getMatcher
argument_list|(
name|s
operator|.
name|substring
argument_list|(
name|SCHEME
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
operator|||
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
comment|// If no match, return original String.
return|return
name|s
return|;
block|}
return|return
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|+
name|reverseHostname
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|+
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Matcher
name|getMatcher
parameter_list|(
specifier|final
name|String
name|u
parameter_list|)
block|{
if|if
condition|(
name|u
operator|==
literal|null
operator|||
name|u
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|URI_RE_PARSER
operator|.
name|matcher
argument_list|(
name|u
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|reverseHostname
parameter_list|(
specifier|final
name|String
name|hostname
parameter_list|)
block|{
if|if
condition|(
name|hostname
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
name|hostname
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|StringTokenizer
name|st
init|=
operator|new
name|StringTokenizer
argument_list|(
name|hostname
argument_list|,
literal|"."
argument_list|,
literal|false
argument_list|)
init|;
name|st
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
name|Object
name|next
init|=
name|st
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|insert
argument_list|(
literal|0
argument_list|,
literal|"."
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|insert
argument_list|(
literal|0
argument_list|,
name|next
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

