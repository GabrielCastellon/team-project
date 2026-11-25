# Proposed MVP flows for owners to confirm

These specifications describe behavior, not implementations. All primary actors are application users. The foundation implements none of these flows.

| Use case / lead | Preconditions | Main success flow | Alternatives / errors | Postcondition |
|---|---|---|---|---|
| Register / log in — Matthew | App is open; registration policy agreed | Enter account details; register or authenticate; reach personal collections | Invalid details, duplicate account, incorrect credentials, unavailable storage | Successful session is associated with a stable user ID |
| Search / browse — Kevin | Search API configured | Enter search terms; see credited results; choose next page / orientation / order | Blank query, invalid page/filter, API rejection/limit, empty results | Current candidate page is available for display/ranking |
| Recommend — Kevin | Preference samples and a candidate page exist | Request recommendations; receive ranked candidates excluding saved samples | Cold start, missing page, incompatible/failed embedding, storage failure | Ranked results displayed without changing saved samples |
| Manage pin — Alex | Account and target board are available (exact policy TBD) | Choose image; enter/edit title, description and tags; save; reopen details | Invalid fields, missing image, unauthorized target, storage failure | Pin metadata is persisted and can be read back |
| Manage board — Gabriel | Account context available | Create board; view it; rename it; delete after confirmation | Invalid/duplicate name policy, missing board, unauthorized access, storage failure | Board collection reflects confirmed action on reload |
| Customize profile — Kieran, coordinating with Matthew | Account selected/authenticated | View current settings; edit profile/appearance; save; reopen | Invalid fields, unsupported asset/settings, storage failure | Accepted settings persist and display correctly |

Kevin's existing module also includes Taste Mixer, group ranking and board-preference analysis. These are his discovery extensions, not implementations of Gabriel's boards or Alex's pins. Confirm with the team that this scope leaves fair contributions for everyone.

## Unresolved contracts

Owners must confirm stable identifiers, account ownership checks, pin-to-photo mapping, JSON schema, deletion behavior and profile/account responsibilities. Use interfaces and adapters to integrate agreed contracts; do not invent working authentication around demo preference IDs.
